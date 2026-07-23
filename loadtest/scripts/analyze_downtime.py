#!/usr/bin/env python3
"""
analyze_downtime.py

Turns a downtime_probe.py CSV into an actual downtime report.

For each endpoint, failures are grouped into contiguous "outage windows".
Because we only sample every `interval` seconds, we report TWO bounds for
each outage's duration:

  - tight_duration_ms   : from the first failed request to the last failed
                           request in the run (a LOWER bound — the real
                           outage could have started/ended slightly outside
                           this, hidden between samples)
  - bounded_duration_ms : from the last successful request BEFORE the run to
                           the first successful request AFTER it (an UPPER
                           bound — guaranteed to contain the real outage)

Report the bounded (upper) figure as your headline "downtime" number —
it's the conservative, defensible claim. Report the interval you probed at
alongside it, since that's the resolution limit (e.g. "downtime was
{X}ms, measured at 20ms probe resolution").

Usage:
    python3 analyze_downtime.py \\
        --csv evaluation/downtime_probe.csv \\
        --out evaluation/downtime_report.txt \\
        --json-out evaluation/downtime_report.json
"""
import argparse
import csv
import json
from collections import defaultdict


def analyze(rows):
    by_endpoint = defaultdict(list)
    for r in rows:
        by_endpoint[r["endpoint"]].append(r)

    report = {}
    for name, reqs in by_endpoint.items():
        reqs.sort(key=lambda r: r["timestamp_ms"])
        total = len(reqs)
        failed_count = sum(1 for r in reqs if r["success"] == 0)

        outages = []
        i = 0
        n = len(reqs)
        while i < n:
            if reqs[i]["success"] == 0:
                start_idx = i
                while i < n and reqs[i]["success"] == 0:
                    i += 1
                end_idx = i  # first success after the run, or n if it never recovers

                tight_start = reqs[start_idx]["timestamp_ms"]
                tight_end = reqs[end_idx - 1]["timestamp_ms"]

                bound_start = reqs[start_idx - 1]["timestamp_ms"] if start_idx > 0 else tight_start
                bound_end = reqs[end_idx]["timestamp_ms"] if end_idx < n else tight_end

                sample_statuses = sorted({str(r["status"]) for r in reqs[start_idx:end_idx]})

                outages.append(
                    {
                        "failed_requests": end_idx - start_idx,
                        "tight_duration_ms": tight_end - tight_start,
                        "bounded_duration_ms": bound_end - bound_start,
                        "window_start_ms": bound_start,
                        "window_end_ms": bound_end,
                        "observed_statuses": sample_statuses,
                        "recovered": end_idx < n,
                    }
                )
            else:
                i += 1

        total_bounded_downtime = sum(o["bounded_duration_ms"] for o in outages)
        total_tight_downtime = sum(o["tight_duration_ms"] for o in outages)
        longest_bounded = max((o["bounded_duration_ms"] for o in outages), default=0)

        report[name] = {
            "total_requests": total,
            "failed_requests": failed_count,
            "success_rate_pct": round(100 * (total - failed_count) / total, 4) if total else None,
            "outage_count": len(outages),
            "total_downtime_ms_tight_lower_bound": total_tight_downtime,
            "total_downtime_ms_bounded_upper_bound": total_bounded_downtime,
            "longest_outage_ms_bounded": longest_bounded,
            "outages": outages,
        }
    return report


def format_report(report, interval_note=""):
    lines = []
    lines.append("=" * 62)
    lines.append("Client-Observed Downtime Report")
    lines.append("=" * 62)
    if interval_note:
        lines.append(interval_note)
    lines.append("")

    grand_total_bounded = 0
    grand_total_tight = 0
    for name, stats in report.items():
        lines.append(f"Endpoint: {name}")
        lines.append(f"  Total requests        : {stats['total_requests']}")
        lines.append(f"  Failed requests       : {stats['failed_requests']}")
        lines.append(f"  Success rate          : {stats['success_rate_pct']}%")
        lines.append(f"  Outage windows        : {stats['outage_count']}")
        lines.append(f"  Downtime (lower bound): {stats['total_downtime_ms_tight_lower_bound']} ms")
        lines.append(f"  Downtime (upper bound): {stats['total_downtime_ms_bounded_upper_bound']} ms  <- report this one")
        lines.append(f"  Longest single outage : {stats['longest_outage_ms_bounded']} ms")
        for idx, o in enumerate(stats["outages"], 1):
            recovered = "recovered" if o["recovered"] else "DID NOT RECOVER before probe stopped"
            lines.append(
                f"    outage #{idx}: {o['failed_requests']} failed reqs, "
                f"statuses={o['observed_statuses']}, "
                f"bounded={o['bounded_duration_ms']}ms, tight={o['tight_duration_ms']}ms, "
                f"{recovered}"
            )
        lines.append("")
        grand_total_bounded += stats["total_downtime_ms_bounded_upper_bound"]
        grand_total_tight += stats["total_downtime_ms_tight_lower_bound"]

    lines.append("-" * 62)
    lines.append(f"Combined downtime across all endpoints, lower bound: {grand_total_tight} ms")
    lines.append(f"Combined downtime across all endpoints, upper bound: {grand_total_bounded} ms")
    lines.append("=" * 62)
    return "\n".join(lines)


def main():
    parser = argparse.ArgumentParser(description=__doc__, formatter_class=argparse.RawDescriptionHelpFormatter)
    parser.add_argument("--csv", required=True, help="input CSV from downtime_probe.py")
    parser.add_argument("--out", required=True, help="path to write the text report")
    parser.add_argument("--json-out", default=None, help="optional path to write the full JSON report")
    args = parser.parse_args()

    with open(args.csv) as f:
        rows = list(csv.DictReader(f))
    if not rows:
        raise SystemExit(f"No rows found in {args.csv} — did the probe actually run?")

    for r in rows:
        r["timestamp_ms"] = int(r["timestamp_ms"])
        r["success"] = int(r["success"])

    # Infer sampling interval from the data for the report note.
    timestamps = sorted(r["timestamp_ms"] for r in rows)
    gaps = [b - a for a, b in zip(timestamps, timestamps[1:]) if b > a]
    approx_interval = min(gaps) if gaps else None
    interval_note = (
        f"(approx. probe resolution: {approx_interval} ms — real outage boundaries"
        f" fall within +/- this of the reported bounds)"
        if approx_interval
        else ""
    )

    report = analyze(rows)
    text = format_report(report, interval_note)

    print(text)
    with open(args.out, "w") as f:
        f.write(text + "\n")

    if args.json_out:
        with open(args.json_out, "w") as f:
            json.dump(report, f, indent=2)


if __name__ == "__main__":
    main()
