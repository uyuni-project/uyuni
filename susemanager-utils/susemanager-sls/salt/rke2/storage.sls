#!jinja|yaml
# Install local-path-provisioner and mark its StorageClass as default.

{%- set kubectl = '/usr/local/bin/kubectl --kubeconfig=/etc/rancher/rke2/rke2.yaml' %}

include:
  - .rke2

rke2_local_path_provisioner_installed:
  cmd.run:
    - name: |
        {{ kubectl }} apply -f https://raw.githubusercontent.com/rancher/local-path-provisioner/v0.0.31/deploy/local-path-storage.yaml
        {{ kubectl }} wait --for=condition=available --timeout=180s \
            deployment/local-path-provisioner -n local-path-storage
        {{ kubectl }} patch storageclass local-path \
            -p '{"metadata":{"annotations":{"storageclass.kubernetes.io/is-default-class":"true"}}}'
    - unless: {{ kubectl }} get sc local-path 2>/dev/null | grep -q '(default)'
    - require:
      - cmd: rke2_traefik_ready
