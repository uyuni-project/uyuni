#!jinja|yaml
# Install RKE2 on the proxy node.
#
# If k3s is detected, performs migration:
#   - etcd mode (multi-node): etcd snapshot → restore into RKE2
#   - SQLite mode (single-node): backup config → stop k3s → fresh RKE2
# If RKE2 is already present and wipe_k8s is true, does a full wipe + reinstall.
# Otherwise, ensures RKE2 is installed and running (idempotent).

{%- set wipe_k8s   = salt['pillar.get']('proxy:rke2:wipe_k8s', False) %}
{%- set kubectl     = '/usr/local/bin/kubectl --kubeconfig=/etc/rancher/rke2/rke2.yaml' %}
{%- set k3s_present = salt['file.file_exists']('/usr/local/bin/k3s') or salt['file.file_exists']('/usr/bin/k3s') or salt['file.file_exists']('/etc/rancher/k3s/k3s.yaml') %}
{%- set k3s_etcd    = salt['file.directory_exists']('/var/lib/rancher/k3s/server/db/etcd') %}

include:
  - .packages
  - .proxy_config

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

{%- endif %}

# ============================================================================
# Common: RKE2 config, install, start, wait
# ============================================================================

/etc/rancher/rke2/config.yaml:
  file.managed:
    - user: root
    - group: root
    - mode: '0644'
    - makedirs: True
    - contents: |
        ingress-controller: traefik
        write-kubeconfig-mode: "0644"

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

# CoreDNS: forward to the host's upstream DNS servers instead of the
# pod's /etc/resolv.conf (which points at CoreDNS itself, creating a loop).
/var/lib/rancher/rke2/server/manifests/rke2-coredns-config.yaml:
  file.managed:
    - user: root
    - group: root
    - mode: '0644'
    - makedirs: True
    - template: jinja
    - contents: |
        apiVersion: helm.cattle.io/v1
        kind: HelmChartConfig
        metadata:
          name: rke2-coredns
          namespace: kube-system
        spec:
          valuesContent: |-
            servers:
            - zones:
              - zone: .
              port: 53
              plugins:
              - name: errors
              - name: health
                configBlock: |-
                  lameduck 10s
              - name: ready
              - name: kubernetes
                parameters: cluster.local in-addr.arpa ip6.arpa
                configBlock: |-
                  pods insecure
                  fallthrough in-addr.arpa ip6.arpa
                  ttl 30
              - name: prometheus
                parameters: 0.0.0.0:9153
              - name: forward
                parameters: . {{ grains['dns']['nameservers'] | join(' ') }}
              - name: cache
                parameters: 30
              - name: reload
              - name: loadbalance

proxy_rke2_rke2_installed:
  cmd.run:
    - name: |
        curl -sfL https://get.rke2.io | sh -
        systemctl daemon-reload
    - creates: /usr/local/bin/rke2
    - require:
      - file: /etc/rancher/rke2/config.yaml
      - file: /var/lib/rancher/rke2/server/manifests/uyuni-traefik.yaml
      - file: /var/lib/rancher/rke2/server/manifests/rke2-coredns-config.yaml
      - pkg: proxy_rke2_pkgs_installed
{%- if k3s_present %}
      - cmd: proxy_rke2_k3s_stop
{%- endif %}

{%- if k3s_present and k3s_etcd %}
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
      - cmd: proxy_rke2_rke2_installed

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

{%- if k3s_present %}
# Uninstall k3s BEFORE starting rke2-server.
# CRITICAL: never run k3s-uninstall while RKE2 is running.
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
      - cmd: proxy_rke2_rke2_installed
{%- endif %}
{%- endif %}

proxy_rke2_rke2_server_running:
  service.running:
    - name: rke2-server
    - enable: True
    - require:
      - cmd: proxy_rke2_rke2_installed
{%- if k3s_present %}
      - cmd: proxy_rke2_k3s_uninstall
{%- endif %}

proxy_rke2_kubectl_symlink:
  cmd.run:
    - name: |
        for _ in $(seq 1 30); do
            if [ -x /var/lib/rancher/rke2/bin/kubectl ]; then
                ln -sf /var/lib/rancher/rke2/bin/kubectl /usr/local/bin/kubectl
                echo "kubectl symlinked"
                exit 0
            fi
            sleep 2
        done
        exit 1
    - creates: /usr/local/bin/kubectl
    - require:
      - service: proxy_rke2_rke2_server_running

proxy_rke2_rke2_node_ready:
  cmd.run:
    - name: |
        for _ in $(seq 1 60); do
            {{ kubectl }} get nodes 2>/dev/null | grep -q ' Ready ' && exit 0
            sleep 5
        done
        exit 1
    - require:
      - cmd: proxy_rke2_kubectl_symlink

# --- Post-migration cleanup (only after k3s migration) ---------------------
{%- if k3s_present %}

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
      - cmd: proxy_rke2_rke2_node_ready

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
      - cmd: proxy_rke2_rke2_node_ready

proxy_rke2_k3s_cleanup_metrics:
  cmd.run:
    - name: |
        {{ kubectl }} delete apiservices.apiregistration.k8s.io v1beta1.metrics.k8s.io 2>/dev/null || true
        echo "Old metrics APIService removed"
    - onlyif: {{ kubectl }} get apiservices.apiregistration.k8s.io v1beta1.metrics.k8s.io 2>/dev/null
    - require:
      - cmd: proxy_rke2_rke2_node_ready

{%- endif %}

proxy_rke2_rke2_traefik_installed:
  cmd.run:
    - name: |
        for _ in $(seq 1 60); do
            {{ kubectl }} -n kube-system get ds rke2-traefik >/dev/null 2>&1 && exit 0
            sleep 5
        done
        exit 1
    - require:
      - cmd: proxy_rke2_rke2_node_ready

proxy_rke2_rke2_traefik_ready:
  cmd.run:
    - name: {{ kubectl }} -n kube-system rollout status ds/rke2-traefik --timeout=300s
    - require:
      - cmd: proxy_rke2_rke2_traefik_installed

proxy_rke2_bashrc_exists:
  file.managed:
    - name: /root/.bashrc
    - replace: False
    - user: root
    - group: root
    - mode: '0644'

proxy_rke2_bashrc_env:
  file.blockreplace:
    - name: /root/.bashrc
    - marker_start: "# >>> rke2 env >>>"
    - marker_end: "# <<< rke2 env <<<"
    - content: |
        export PATH=$PATH:/var/lib/rancher/rke2/bin
        export KUBECONFIG=/etc/rancher/rke2/rke2.yaml
        export CONTAINERD_ADDRESS=/run/k3s/containerd/containerd.sock
        export CRI_CONFIG_FILE=/var/lib/rancher/rke2/agent/etc/crictl.yaml
    - append_if_not_found: True
    - append_newline: True
    - backup: '.bak'
    - require:
      - file: proxy_rke2_bashrc_exists
      - service: proxy_rke2_rke2_server_running
