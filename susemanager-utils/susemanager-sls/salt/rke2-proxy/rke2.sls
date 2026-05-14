#!jinja|yaml
# Pre-stage RKE2 config + Traefik HelmChartConfig, install RKE2,
# wait for the cluster + Traefik DaemonSet to be ready,
# and persist the kubectl/crictl env in /root/.bashrc.

{%- set wipe_rke2 = salt['pillar.get']('rke2_proxy:wipe_rke2', False) %}
{%- set kubectl   = '/usr/local/bin/kubectl --kubeconfig=/etc/rancher/rke2/rke2.yaml' %}

include:
  - .packages
  - .proxy_config

{%- if wipe_rke2 %}
rke2_proxy_rke2_wipe:
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
      - cmd: rke2_proxy_backup_config
{%- endif %}

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

rke2_proxy_rke2_installed:
  cmd.run:
    - name: curl -sfL https://get.rke2.io | sh -
    - creates: /usr/local/bin/rke2
    - require:
      - file: /etc/rancher/rke2/config.yaml
      - file: /var/lib/rancher/rke2/server/manifests/uyuni-traefik.yaml
      - pkg: rke2_proxy_pkgs_installed

rke2_proxy_rke2_server_running:
  service.running:
    - name: rke2-server
    - enable: True
    - require:
      - cmd: rke2_proxy_rke2_installed

# RKE2 ships kubectl under /var/lib/rancher/rke2/bin which isn't on PATH.
# Expose it system-wide so the rest of the states (and the admin) can use
# `kubectl` without sourcing the bashrc env block. Done before any state
# that invokes kubectl, since the kubectl variable resolves to this path.
rke2_proxy_kubectl_symlink:
  file.symlink:
    - name: /usr/local/bin/kubectl
    - target: /var/lib/rancher/rke2/bin/kubectl
    - force: True
    - require:
      - cmd: rke2_proxy_rke2_installed

rke2_proxy_rke2_node_ready:
  cmd.run:
    - name: |
        for _ in $(seq 1 60); do
            {{ kubectl }} get nodes 2>/dev/null | grep -q ' Ready ' && exit 0
            sleep 5
        done
        exit 1
    - require:
      - service: rke2_proxy_rke2_server_running
      - file: rke2_proxy_kubectl_symlink

rke2_proxy_rke2_traefik_installed:
  cmd.run:
    - name: |
        for _ in $(seq 1 60); do
            {{ kubectl }} -n kube-system get ds rke2-traefik >/dev/null 2>&1 && exit 0
            sleep 5
        done
        exit 1
    - require:
      - cmd: rke2_proxy_rke2_node_ready

rke2_proxy_rke2_traefik_ready:
  cmd.run:
    - name: {{ kubectl }} -n kube-system rollout status ds/rke2-traefik --timeout=300s
    - require:
      - cmd: rke2_proxy_rke2_traefik_installed

rke2_proxy_bashrc_env:
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
