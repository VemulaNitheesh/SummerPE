#!/usr/bin/env bash
# run_full_evaluation.sh
#
# Runs a full blue-green evaluation:
#   1. Starts a Python downtime probe hitting all 3 public endpoints
#      every 20ms (fine-grained, catches individual dropped requests).
#   2. Starts wrk2 generating sustained, constant-rate mixed load in the
#      background (realistic throughput/latency numbers).
#   3. Runs the given Ansible playbook (deploy-blue-green.yml by default)
#      and times the whole thing.
#   4. Stops the probe and wrk2, analyzes results, and writes one combined
#      report covering:
#        - total deployment wall time (build + health-check + switch + drain)
#        - Nginx upstream-switch time alone (from Ansible)
#        - actual client-observed downtime per endpoint (from the probe)
#        - wrk2 throughput/latency percentiles under the switch
#
# Usage:
#   cd wrk2
#   ./run_full_evaluation.sh                    # runs deploy-blue-green.yml
#   ./run_full_evaluation.sh switch.yml          # runs switch.yml instead
#
# Tunables (env vars, all optional):
#   WRK2_DURATION=180   ceiling in seconds for probe/wrk2 (we stop early anyway)
#   WRK2_THREADS=4
#   WRK2_CONNECTIONS=20
#   WRK2_RATE=50         requests/sec, constant (this is wrk2's whole point)
#   GATEWAY_URL=http://127.0.0.1:8090

set -uo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
ROOT_DIR="$(cd "$SCRIPT_DIR/.." && pwd)"
EVAL_DIR="$ROOT_DIR/evaluation"
mkdir -p "$EVAL_DIR"

PLAYBOOK="${1:-deploy-blue-green.yml}"
GATEWAY_URL="${GATEWAY_URL:-http://127.0.0.1:8090}"
WRK2_BIN="$SCRIPT_DIR/wrk"
WRK2_DURATION="${WRK2_DURATION:-90}"
WRK2_THREADS="${WRK2_THREADS:-4}"
WRK2_CONNECTIONS="${WRK2_CONNECTIONS:-20}"
WRK2_RATE="${WRK2_RATE:-50}"

TIMESTAMP=$(date +%Y%m%d_%H%M%S)
PROBE_CSV="$EVAL_DIR/downtime_probe_${TIMESTAMP}.csv"
DOWNTIME_REPORT="$EVAL_DIR/downtime_report_${TIMESTAMP}.txt"
DOWNTIME_JSON="$EVAL_DIR/downtime_report_${TIMESTAMP}.json"
WRK2_LOG="$EVAL_DIR/wrk2_during_switch_${TIMESTAMP}.txt"
FULL_REPORT="$EVAL_DIR/full_evaluation_${TIMESTAMP}.txt"

echo "=================================================="
echo " Blue-Green Downtime Evaluation"
echo "=================================================="
echo " Playbook under test : $PLAYBOOK"
echo " Gateway URL          : $GATEWAY_URL"
echo " Run ID                : $TIMESTAMP"
echo "=================================================="
echo

echo "[1/5] Starting downtime probe (20ms resolution, all 3 endpoints)..."
python3 "$SCRIPT_DIR/scripts/downtime_probe.py" \
  --duration "$WRK2_DURATION" \
  --interval 0.02 \
  --endpoint "product=$GATEWAY_URL/actuator/product/health" \
  --endpoint "inventory=$GATEWAY_URL/actuator/inventory/health" \
  --endpoint "order=$GATEWAY_URL/actuator/order/health" \
  --out "$PROBE_CSV" &
PROBE_PID=$!

echo "[2/5] Starting wrk2 sustained mixed load (${WRK2_RATE} req/s constant)..."
WRK2_PID=""
if [ -x "$WRK2_BIN" ]; then
  "$WRK2_BIN" -t"$WRK2_THREADS" -c"$WRK2_CONNECTIONS" -d"${WRK2_DURATION}s" -R"$WRK2_RATE" -L    -H "Connection: close" \
    -s "$SCRIPT_DIR/scripts/mixed_workload.lua" \
    "$GATEWAY_URL" > "$WRK2_LOG" 2>&1 &
  WRK2_PID=$!
else
  echo "  wrk2 binary not found at $WRK2_BIN"
  echo "  Run ./build_wrk2.sh first. Continuing with the Python probe only."
fi

echo "[3/5] Letting baseline traffic stabilize for 5s before triggering the switch..."
sleep 5

echo "[4/5] Running ansible-playbook $PLAYBOOK ..."
DEPLOY_START_MS=$(date +%s%3N)
ansible-playbook -i "$ROOT_DIR/ansible/inventory.ini" "$ROOT_DIR/ansible/$PLAYBOOK"
DEPLOY_STATUS=$?
DEPLOY_END_MS=$(date +%s%3N)
DEPLOY_DURATION_MS=$((DEPLOY_END_MS - DEPLOY_START_MS))

echo "    Deploy finished (exit $DEPLOY_STATUS) in ${DEPLOY_DURATION_MS} ms wall time."
echo "    Capturing 5 more seconds of post-switch traffic..."
sleep 5

echo "[5/5] Stopping probe/wrk2 and analyzing results..."
kill "$PROBE_PID" 2>/dev/null
wait "$PROBE_PID" 2>/dev/null
if [ -n "$WRK2_PID" ]; then
  echo "Waiting for wrk2 to finish..."
  wait "$WRK2_PID"
fi
python3 "$SCRIPT_DIR/scripts/analyze_downtime.py" \
  --csv "$PROBE_CSV" \
  --out "$DOWNTIME_REPORT" \
  --json-out "$DOWNTIME_JSON"

{
  echo "=================================================="
  echo "FULL BLUE-GREEN EVALUATION REPORT"
  echo "=================================================="
  echo "Run ID               : $TIMESTAMP"
  echo "Playbook executed     : $PLAYBOOK"
  echo "Deploy exit status    : $DEPLOY_STATUS"
  echo "Deploy wall time       : ${DEPLOY_DURATION_MS} ms"
  echo "  (build + health-check + traffic switch + drain + cleanup —"
  echo "   this is NOT the same thing as downtime, see below)"
  echo
  echo "--- Nginx switch time only (upstream flip + reload, from Ansible) ---"
  if [ -f "$EVAL_DIR/traffic_switch_results.txt" ]; then
    cat "$EVAL_DIR/traffic_switch_results.txt"
  else
    echo "  (traffic_switch_results.txt not found — did the deploy playbook run to completion?)"
  fi
  echo
  echo "--- Client-observed downtime (request-level probe, 20ms resolution) ---"
  cat "$DOWNTIME_REPORT"
  echo
  echo "--- wrk2 sustained mixed-load summary (throughput + latency percentiles) ---"
  if [ -f "$WRK2_LOG" ]; then
    cat "$WRK2_LOG"
  else
    echo "  (wrk2 was not run — binary missing, see wrk2/build_wrk2.sh)"
  fi
} > "$FULL_REPORT"

echo
echo "=================================================="
echo "Done."
echo "Full report : $FULL_REPORT"
echo "Raw probe   : $PROBE_CSV"
echo "Downtime JSON: $DOWNTIME_JSON"
echo "=================================================="
