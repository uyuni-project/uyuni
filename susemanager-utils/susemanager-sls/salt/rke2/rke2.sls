#!jinja|yaml
# Install, configure and start RKE2 (generic — no proxy or k3s logic).
#
# Consumers can inject pre-install dependencies (e.g. k3s stop, wipe)
# via require_in on the rke2_installed state.

{%- set kubectl = '/usr/local/bin/kubectl --kubeconfig=/etc/rancher/rke2/rke2.yaml' %}

include:
  - .packages

/etc/rancher/rke2/config.yaml:
  file.managed:
    - user: root
    - group: root
    - mode: '0644'
    - makedirs: True
    - contents: |
        ingress-controller: traefik
        write-kubeconfig-mode: "0644"

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

rke2_installed:
  cmd.run:
    - name: |
        curl -sfL https://get.rke2.io | sh -
        systemctl daemon-reload
    - creates: /usr/local/bin/rke2
    - require:
      - file: /etc/rancher/rke2/config.yaml
      - file: /var/lib/rancher/rke2/server/manifests/rke2-coredns-config.yaml
      - pkg: rke2_pkgs_installed

rke2_server_running:
  service.running:
    - name: rke2-server
    - enable: True
    - require:
      - cmd: rke2_installed

rke2_kubectl_symlink:
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
      - service: rke2_server_running

rke2_node_ready:
  cmd.run:
    - name: |
        for _ in $(seq 1 60); do
            {{ kubectl }} get nodes 2>/dev/null | grep -q ' Ready ' && exit 0
            sleep 5
        done
        exit 1
    - require:
      - cmd: rke2_kubectl_symlink

rke2_traefik_installed:
  cmd.run:
    - name: |
        for _ in $(seq 1 60); do
            {{ kubectl }} -n kube-system get ds rke2-traefik >/dev/null 2>&1 && exit 0
            sleep 5
        done
        exit 1
    - require:
      - cmd: rke2_node_ready

rke2_traefik_ready:
  cmd.run:
    - name: {{ kubectl }} -n kube-system rollout status ds/rke2-traefik --timeout=300s
    - require:
      - cmd: rke2_traefik_installed

rke2_bashrc_exists:
  file.managed:
    - name: /root/.bashrc
    - replace: False
    - user: root
    - group: root
    - mode: '0644'

rke2_bashrc_env:
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
      - file: rke2_bashrc_exists
      - service: rke2_server_running
