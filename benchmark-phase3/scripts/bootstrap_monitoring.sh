#!/usr/bin/env bash
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
# shellcheck source=common.sh
source "$SCRIPT_DIR/common.sh"

usage() {
  cat <<USAGE
Usage: bash benchmark-phase3/scripts/bootstrap_monitoring.sh [--session-dir <path>]

Installs/updates monitoring stack and applies ServiceMonitor resources for Uyuni and DB services.
USAGE
}

SESSION_DIR=""
while [[ $# -gt 0 ]]; do
  case "$1" in
    --session-dir)
      SESSION_DIR="$2"
      shift 2
      ;;
    -h|--help)
      usage
      exit 0
      ;;
    *)
      die "Unknown argument: $1"
      ;;
  esac
done

phase3_load_env
require_cmd kubectl helm jq

if [[ -z "$SESSION_DIR" ]]; then
  SESSION_DIR="$(phase3_new_session_dir "$PHASE3_ROOT/results" "monitoring" "$STORAGE_CLASS")"
fi
mkdir -p "$SESSION_DIR/monitoring"
LOG_FILE="$SESSION_DIR/monitoring/bootstrap-monitoring.log"

{
  log "Bootstrap monitoring start"
  log "Session: $SESSION_DIR"

  kubectl get ns "$MONITORING_NAMESPACE" >/dev/null 2>&1 || kubectl create ns "$MONITORING_NAMESPACE"

  if ! helm repo list | awk '{print $1}' | grep -q '^prometheus-community$'; then
    helm repo add prometheus-community https://prometheus-community.github.io/helm-charts
  fi
  helm repo update

  helm upgrade --install kube-prom-stack prometheus-community/kube-prometheus-stack \
    -n "$MONITORING_NAMESPACE" \
    --create-namespace \
    --set prometheus.prometheusSpec.serviceMonitorSelectorNilUsesHelmValues=false \
    --set prometheus.prometheusSpec.podMonitorSelectorNilUsesHelmValues=false \
    --wait

  # CRDs are required for ServiceMonitor/PodMonitor resources.
  kubectl get crd servicemonitors.monitoring.coreos.com >/dev/null 2>&1 \
    || die "ServiceMonitor CRD is missing after monitoring install"
  kubectl get crd podmonitors.monitoring.coreos.com >/dev/null 2>&1 \
    || die "PodMonitor CRD is missing after monitoring install"

  # Detect candidate services dynamically and label them for selector-based monitors.
  local_services_json="$(kubectl -n "$UYUNI_NAMESPACE" get svc -o json)"

  uyuni_service="$(echo "$local_services_json" | jq -r '.items[].metadata.name' | grep -E '(web|tomcat|uyuni)' | head -n1 || true)"
  db_service="$(echo "$local_services_json" | jq -r '.items[].metadata.name' | grep -E '^(db|postgres|pgsql|reportdb)' | head -n1 || true)"

  [[ -n "$uyuni_service" ]] || die "Could not detect a Uyuni service in namespace $UYUNI_NAMESPACE"
  [[ -n "$db_service" ]] || die "Could not detect a DB service in namespace $UYUNI_NAMESPACE"

  uyuni_port="$(kubectl -n "$UYUNI_NAMESPACE" get svc "$uyuni_service" -o jsonpath='{.spec.ports[0].port}')"
  db_port="$(kubectl -n "$UYUNI_NAMESPACE" get svc "$db_service" -o jsonpath='{.spec.ports[0].port}')"

  [[ -n "$uyuni_port" ]] || die "Failed to detect target port for service $uyuni_service"
  [[ -n "$db_port" ]] || die "Failed to detect target port for service $db_service"

  kubectl -n "$UYUNI_NAMESPACE" label svc "$uyuni_service" benchmark-phase3-target=uyuni --overwrite
  kubectl -n "$UYUNI_NAMESPACE" label svc "$db_service" benchmark-phase3-target=db --overwrite

  # Apply ServiceMonitors with dynamic ports and configured namespaces.
  cat <<YAML | kubectl apply -f -
apiVersion: monitoring.coreos.com/v1
kind: ServiceMonitor
metadata:
  name: uyuni-phase3
  namespace: ${MONITORING_NAMESPACE}
  labels:
    app.kubernetes.io/name: benchmark-phase3
spec:
  namespaceSelector:
    matchNames:
      - ${UYUNI_NAMESPACE}
  selector:
    matchLabels:
      benchmark-phase3-target: uyuni
  endpoints:
    - interval: 15s
      scrapeTimeout: 10s
      path: /metrics
      scheme: http
      targetPort: ${uyuni_port}
---
apiVersion: monitoring.coreos.com/v1
kind: ServiceMonitor
metadata:
  name: uyuni-db-phase3
  namespace: ${MONITORING_NAMESPACE}
  labels:
    app.kubernetes.io/name: benchmark-phase3
spec:
  namespaceSelector:
    matchNames:
      - ${UYUNI_NAMESPACE}
  selector:
    matchLabels:
      benchmark-phase3-target: db
  endpoints:
    - interval: 15s
      scrapeTimeout: 10s
      path: /metrics
      scheme: http
      targetPort: ${db_port}
YAML

  # Optional PodMonitor manifest can be useful when pods expose metrics directly.
  kubectl apply -f "$PHASE3_ROOT/manifests/monitoring/podmonitor-optional.yaml" \
    --namespace "$MONITORING_NAMESPACE"

  # Save installed resources for evidence.
  kubectl -n "$MONITORING_NAMESPACE" get servicemonitors,podmonitors -o wide \
    > "$SESSION_DIR/monitoring/monitoring-resources.txt"

  {
    echo "uyuni_service=$uyuni_service"
    echo "uyuni_target_port=$uyuni_port"
    echo "db_service=$db_service"
    echo "db_target_port=$db_port"
  } > "$SESSION_DIR/monitoring/detected-targets.txt"

  log "Monitoring bootstrap complete"
} | tee "$LOG_FILE"
