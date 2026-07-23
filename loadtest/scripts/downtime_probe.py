#!/usr/bin/env python3
"""
downtime_probe.py

Continuously polls one or more HTTP endpoints at a fixed interval and logs
every single request's outcome with a millisecond timestamp. This is what
actually measures "downtime" as your client would experience it — separate
from and more precise than:
  - deployment time (how long the whole Ansible run takes)
  - Nginx reload/switch time (how long the upstream flip + reload takes)
A switch can complete in under 2 seconds while still dropping a handful of
in-flight requests; this script is what catches that.

No external dependencies — uses only the Python standard library, so it
runs anywhere Python 3 is installed.

Usage:
    python3 downtime_probe.py \\
        --duration 120 \\
        --interval 0.02 \\
        --endpoint "products=http://127.0.0.1:8090/api/v1/products" \\
        --endpoint "inventory=http://127.0.0.1:8090/api/v1/inventories" \\
        --endpoint "orders=http://127.0.0.1:8090/api/v1/orders" \\
        --out evaluation/downtime_probe.csv

Stop early any time with Ctrl+C (or SIGTERM) — partial results are still
written to --out.
"""
import argparse
import csv
import signal
import socket
import threading
import time
import urllib.error
import urllib.request


def probe_endpoint(name, url, interval, timeout, stop_event, rows, lock):
    while not stop_event.is_set():
        t0 = time.time()
        status = 0
        error = ""
        try:
            req = urllib.request.Request(url, method="GET")
            with urllib.request.urlopen(req, timeout=timeout) as resp:
                status = resp.status
        except urllib.error.HTTPError as e:
            # Server responded, but with an error status (e.g. 502/503/504
            # from Nginx while the upstream is mid-switch).
            status = e.code
            error = f"HTTPError:{e.code}"
        except (urllib.error.URLError, socket.timeout, ConnectionError, OSError) as e:
            # Connection refused / reset / timed out — the gateway or
            # upstream wasn't reachable at all for this request.
            status = 0
            error = f"{type(e).__name__}:{e}"
        except Exception as e:  # noqa: BLE001 - want to log any surprise too
            status = 0
            error = f"{type(e).__name__}:{e}"
        t1 = time.time()

        with lock:
            rows.append(
                {
                    "timestamp_ms": int(t0 * 1000),
                    "endpoint": name,
                    "status": status,
                    "latency_ms": round((t1 - t0) * 1000, 2),
                    "success": 1 if 200 <= status < 400 else 0,
                    "error": error,
                }
            )

        elapsed = t1 - t0
        stop_event.wait(max(0.0, interval - elapsed))


def main():
    parser = argparse.ArgumentParser(description=__doc__, formatter_class=argparse.RawDescriptionHelpFormatter)
    parser.add_argument("--duration", type=float, default=90, help="max seconds to run (default: 90)")
    parser.add_argument("--interval", type=float, default=0.02, help="seconds between requests per endpoint (default: 0.02 = 50/sec)")
    parser.add_argument("--timeout", type=float, default=2.0, help="per-request timeout in seconds (default: 2.0)")
    parser.add_argument("--endpoint", action="append", required=True, metavar="name=url", help="repeatable, e.g. product=http://127.0.0.1:8090/actuator/product/health")
    parser.add_argument("--out", required=True, help="path to write the CSV log")
    args = parser.parse_args()

    rows = []
    lock = threading.Lock()
    stop_event = threading.Event()
    threads = []

    def handle_signal(signum, frame):  # noqa: ARG001
        print(f"\n[downtime_probe] Received signal {signum}, stopping early...")
        stop_event.set()

    signal.signal(signal.SIGINT, handle_signal)
    signal.signal(signal.SIGTERM, handle_signal)

    for spec in args.endpoint:
        if "=" not in spec:
            raise SystemExit(f"--endpoint must be name=url, got: {spec}")
        name, url = spec.split("=", 1)
        t = threading.Thread(
            target=probe_endpoint,
            args=(name, url, args.interval, args.timeout, stop_event, rows, lock),
            daemon=True,
        )
        threads.append(t)
        t.start()

    print(f"[downtime_probe] Probing {len(threads)} endpoint(s) every {args.interval}s for up to {args.duration}s ...")
    print(f"[downtime_probe] Writing to {args.out}")

    stop_event.wait(args.duration)
    stop_event.set()
    for t in threads:
        t.join(timeout=3)

    with lock:
        rows.sort(key=lambda r: r["timestamp_ms"])
        with open(args.out, "w", newline="") as f:
            writer = csv.DictWriter(f, fieldnames=["timestamp_ms", "endpoint", "status", "latency_ms", "success", "error"])
            writer.writeheader()
            writer.writerows(rows)

    total = len(rows)
    failed = sum(1 for r in rows if r["success"] == 0)
    print(f"[downtime_probe] Done. {total} requests logged, {failed} failed ({(100*failed/total if total else 0):.3f}%).")


if __name__ == "__main__":
    main()
