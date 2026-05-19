#!jinja|yaml
# Proxy-specific RKE2 setup: Traefik port config, k3s migration, wipe.
#
# Generic RKE2 install/start/wait logic lives in rke2.rke2 (salt/rke2/).
# This file injects ordering constraints via require_in so that k3s stop
# and wipe run before the generic install.

{%- set wipe_k8s    = salt['pillar.get']('proxy:rke2:wipe_k8s', False) %}
{%- set kubectl     = '/usr/local/bin/kubectl --kubeconfig=/etc/rancher/rke2/rke2.yaml' %}
{%- set k3s_present = salt['file.file_exists']('/usr/local/bin/k3s') or salt['file.file_exists']('/usr/bin/k3s') or salt['file.file_exists']('/etc/rancher/k3s/k3s.yaml') %}
{%- set k3s_etcd    = salt['file.directory_exists']('/var/lib/rancher/k3s/server/db/etcd') %}

include:
  - rke2.rke2
  - .proxy_config

# Traefik HelmChartConfig with proxy-specific ports (SSH, Salt).
# Must be on disk before rke2-server starts so the helm controller picks it up.
/var/lib/rancher/rke2/server/manifests/uyuni-traefik.yaml:
  file.managed:
    - user: root
    - group: root
    - mode: '0644'
    - makedirs: True
    - contents: |
        apiVersion: helm.cattle.io/v1
        kind: HelmChartConfig
        metadata:
          name: rke2-traefik
          namespace: kube-system
        spec:
          valuesContent: |-
            logs:
              general:
                level: "DEBUG"
            ports:
              ssh:
                port: 8022
                expose:
                  default: true
                exposedPort: 8022
                protocol: TCP
                hostPort: 8022
              salt-publish:
                port: 4505
                expose:
                  default: true
                exposedPort: 4505
                protocol: TCP
                hostPort: 4505
                containerPort: 4505
              salt-request:
                port: 4506
                expose:
                  default: true
                exposedPort: 4506
                protocol: TCP
                hostPort: 4506
                containerPort: 4506
    - require_in:
      - cmd: rke2_installed

# ============================================================================
# Path A: k3s → RKE2 migration
# ============================================================================
{%- if k3s_present %}

proxy_rke2_k3s_scale_down:
  cmd.run:
    - name: |
        for ns in $(k3s kubectl get ns -o jsonpath='{.items[*].metadata.name}' 2>/dev/null); do
            case "$ns" in kube-system|kube-public|kube-node-lease|default) continue;; esac
            k3s kubectl -n "$ns" scale deploy --all --replicas=0 2>/dev/null || true
        done
        echo "Scaled down workloads"
    - onlyif: test -f /etc/rancher/k3s/k3s.yaml
    - require:
      - cmd: proxy_rke2_backup_config

{%- if k3s_etcd %}
proxy_rke2_k3s_snapshot:
  cmd.run:
    - name: k3s etcd-snapshot save --name rke2-migration
    - require:
      - cmd: proxy_rke2_k3s_scale_down

proxy_rke2_k3s_snapshot_saved:
  cmd.run:
    - name: |
        mkdir -p /var/backups/k3s-migration
        SNAP=$(ls -1t /var/lib/rancher/k3s/server/db/snapshots/rke2-migration* 2>/dev/null | head -1)
        if [ -z "$SNAP" ]; then
            echo "ERROR: snapshot not found"; exit 1
        fi
        cp "$SNAP" /var/backups/k3s-migration/
        cp /var/lib/rancher/k3s/server/token /var/backups/k3s-migration/token
        echo "Snapshot and token saved to /var/backups/k3s-migration/"
    - require:
      - cmd: proxy_rke2_k3s_snapshot
{%- endif %}

proxy_rke2_k3s_stop:
  cmd.run:
    - name: |
        systemctl disable k3s 2>/dev/null || true
        systemctl disable k3s-server 2>/dev/null || true
        systemctl disable k3s-agent 2>/dev/null || true
        systemctl stop k3s k3s-server k3s-agent 2>/dev/null || true
        for p in /usr/local/bin/k3s-killall.sh /usr/bin/k3s-killall.sh; do
            [ -x "$p" ] && { $p 2>/dev/null || true; $p 2>/dev/null || true; break; }
        done
    - require:
{%- if k3s_etcd %}
      - cmd: proxy_rke2_k3s_snapshot_saved
{%- else %}
      - cmd: proxy_rke2_k3s_scale_down
{%- endif %}
    - require_in:
      - cmd: rke2_installed

{%- if k3s_etcd %}
# --- etcd mode: restore + credential cleanup after RKE2 install ------------

proxy_rke2_k3s_restore:
  cmd.run:
    - name: |
        SNAP=$(ls -1t /var/backups/k3s-migration/rke2-migration* 2>/dev/null | head -1)
        TOKEN=$(cat /var/backups/k3s-migration/token)
        rke2 server \
            --cluster-reset \
            --cluster-reset-restore-path="$SNAP" \
            --token "$TOKEN" 2>&1 | tee /var/log/rke2-migration-restore.log
    - require:
      - cmd: rke2_installed

proxy_rke2_k3s_fix_creds:
  cmd.run:
    - name: rm -f /var/lib/rancher/rke2/server/cred/passwd
    - onlyif: test -f /var/lib/rancher/rke2/server/cred/passwd
    - require:
      - cmd: proxy_rke2_k3s_restore

proxy_rke2_k3s_reset_cleanup:
  cmd.run:
    - name: rm -f /var/lib/rancher/rke2/server/db/reset-flag
    - onlyif: test -f /var/lib/rancher/rke2/server/db/reset-flag
    - require:
      - cmd: proxy_rke2_k3s_fix_creds
{%- endif %}

# CRITICAL: uninstall k3s BEFORE starting rke2-server.
proxy_rke2_k3s_uninstall:
  cmd.run:
    - name: |
        for p in /usr/local/bin/k3s-uninstall.sh /usr/bin/k3s-uninstall.sh; do
            [ -x "$p" ] && { $p; break; }
        done
    - onlyif: test -x /usr/local/bin/k3s-uninstall.sh -o -x /usr/bin/k3s-uninstall.sh
    - require:
{%- if k3s_etcd %}
      - cmd: proxy_rke2_k3s_reset_cleanup
{%- else %}
      - cmd: rke2_installed
{%- endif %}
    - require_in:
      - service: rke2_server_running

# --- Post-migration cleanup -------------------------------------------------

proxy_rke2_k3s_remove_taints:
  cmd.run:
    - name: |
        NODE=$({{ kubectl }} get nodes -o jsonpath='{.items[0].metadata.name}')
        {{ kubectl }} taint node "$NODE" node.kubernetes.io/not-ready:NoSchedule- 2>/dev/null || true
        {{ kubectl }} taint node "$NODE" node.kubernetes.io/not-ready:NoExecute- 2>/dev/null || true
        {{ kubectl }} annotate node "$NODE" k3s.io/hostname- k3s.io/internal-ip- k3s.io/node-args- 2>/dev/null || true
        {{ kubectl }} label node "$NODE" node.kubernetes.io/instance-type- 2>/dev/null || true
        echo "Removed stale k3s taints and annotations"
    - require:
      - cmd: rke2_node_ready

proxy_rke2_k3s_cleanup_coredns:
  cmd.run:
    - name: |
        {{ kubectl }} -n kube-system scale deploy coredns --replicas=0 2>/dev/null || true
        {{ kubectl }} -n kube-system delete svc kube-dns 2>/dev/null || true
        echo "Old CoreDNS cleaned up"
    - onlyif: >-
        {{ kubectl }} -n kube-system get deploy coredns -o jsonpath='{.metadata.labels.app}' 2>/dev/null
        | grep -qv rke2
    - require:
      - cmd: rke2_node_ready

proxy_rke2_k3s_cleanup_metrics:
  cmd.run:
    - name: |
        {{ kubectl }} delete apiservices.apiregistration.k8s.io v1beta1.metrics.k8s.io 2>/dev/null || true
        echo "Old metrics APIService removed"
    - onlyif: {{ kubectl }} get apiservices.apiregistration.k8s.io v1beta1.metrics.k8s.io 2>/dev/null
    - require:
      - cmd: rke2_node_ready

# ============================================================================
# Path B: RKE2 wipe + reinstall (no k3s)
# ============================================================================
{%- elif wipe_k8s %}

proxy_rke2_rke2_wipe:
  cmd.run:
    - name: |
        if [ -x /usr/local/bin/rke2-uninstall.sh ]; then /usr/local/bin/rke2-uninstall.sh
        elif [ -x /usr/bin/rke2-uninstall.sh ]; then /usr/bin/rke2-uninstall.sh
        fi
        rm -rf /var/lib/rancher /etc/rancher /var/lib/kubelet /var/lib/cni /run/k3s /run/flannel || true
        ip -br link 2>/dev/null | awk '/cni|cali|flannel|kube/{print $1}' | cut -d@ -f1 \
            | xargs -r -n1 ip link delete 2>/dev/null || true
        iptables  -F      2>/dev/null || true; iptables  -X      2>/dev/null || true
        iptables  -t nat -F 2>/dev/null || true; iptables  -t nat -X 2>/dev/null || true
        nft flush ruleset 2>/dev/null || true
        systemctl daemon-reload || true
    - onlyif: test -x /usr/local/bin/rke2-uninstall.sh -o -x /usr/bin/rke2-uninstall.sh
    - require:
      - cmd: proxy_rke2_backup_config
    - require_in:
      - cmd: rke2_installed

{%- endif %}
