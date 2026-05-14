#!jinja|yaml
# Create the proxy namespace, the TLS Secret + CA ConfigMap from the extracted certs,
# helm-install the proxy-helm chart and wait for the pods to be ready.

{%- set namespace     = salt['pillar.get']('rke2_proxy:namespace', 'uyuni-proxy') %}
{%- set chart_oci     = salt['pillar.get']('rke2_proxy:chart_oci') %}
{%- set chart_version = salt['pillar.get']('rke2_proxy:chart_version') %}
{%- set image_repo    = salt['pillar.get']('rke2_proxy:image_repo') %}
{%- set image_tag     = salt['pillar.get']('rke2_proxy:image_tag') %}
{%- set work_dir      = salt['grains.get']('rke2_proxy:work_dir', '/tmp/rke2-proxy-config') %}
{%- set tftp_hostnet  = salt['pillar.get']('rke2_proxy:enable_tftp_hostnetwork', True) %}

{%- set kubectl = '/usr/local/bin/kubectl --kubeconfig=/etc/rancher/rke2/rke2.yaml' %}
{%- set helm    = '/usr/bin/helm --kubeconfig=/etc/rancher/rke2/rke2.yaml' %}

include:
  - .proxy_config
  - .storage

rke2_proxy_namespace:
  cmd.run:
    - name: {{ kubectl }} create namespace {{ namespace }} --dry-run=client -o yaml | {{ kubectl }} apply -f -
    - unless: {{ kubectl }} get ns {{ namespace }}
    - require:
      - cmd: rke2_proxy_rke2_traefik_ready

rke2_proxy_cert_secret:
  cmd.run:
    - name: |
        {{ kubectl }} -n {{ namespace }} delete secret proxy-cert --ignore-not-found
        {{ kubectl }} -n {{ namespace }} create secret tls proxy-cert \
            --cert={{ work_dir }}/server.crt --key={{ work_dir }}/server.key
    - unless: |
        {{ kubectl }} -n {{ namespace }} get secret proxy-cert -o jsonpath='{.data.tls\.crt}' \
            | base64 -d | grep -q "BEGIN CERTIFICATE"
    - require:
      - cmd: rke2_proxy_config_extracted
      - cmd: rke2_proxy_namespace

rke2_proxy_ca_configmap:
  cmd.run:
    - name: |
        {{ kubectl }} -n {{ namespace }} delete configmap uyuni-ca --ignore-not-found
        {{ kubectl }} -n {{ namespace }} create configmap uyuni-ca \
            --from-file=ca.crt={{ work_dir }}/ca.crt
    - unless: {{ kubectl }} -n {{ namespace }} get cm uyuni-ca
    - require:
      - cmd: rke2_proxy_config_extracted
      - cmd: rke2_proxy_namespace

rke2_proxy_helm_release:
  cmd.run:
    - name: |
        {{ helm }} install srke2-proxy {{ chart_oci }} \
            --version {{ chart_version }} \
            -n {{ namespace }} \
            --description "Proxy installation" \
            --set repository={{ image_repo }} \
            --set tag={{ image_tag }} \
            --set ingress.class=traefik \
{%- if tftp_hostnet %}
            --set tftp.hostNetwork=true \
{%- endif %}
            --set-file global.config={{ work_dir }}/config.yaml \
            --set-file global.ssh={{ work_dir }}/ssh.yaml \
            --set-file global.httpd={{ work_dir }}/httpd.yaml
    - unless: {{ helm }} -n {{ namespace }} list | grep -q srke2-proxy
    - require:
      - cmd: rke2_proxy_cert_secret
      - cmd: rke2_proxy_ca_configmap
      - cmd: rke2_proxy_local_path_provisioner_installed
      - pkg: rke2_proxy_pkgs_installed

rke2_proxy_pods_ready:
  cmd.run:
    - name: |
        {{ kubectl }} -n {{ namespace }} rollout status deploy/uyuni-proxy      --timeout=300s
        {{ kubectl }} -n {{ namespace }} rollout status deploy/uyuni-proxy-tftp --timeout=300s
    - require:
      - cmd: rke2_proxy_helm_release

rke2_proxy_config_cleanup:
  file.absent:
    - name: {{ work_dir }}
    - require:
      - cmd: rke2_proxy_pods_ready

rke2_proxy_grain_cleanup:
  module.run:
    - name: grains.delkey
    - key: rke2_proxy:work_dir
    - require:
      - file: rke2_proxy_config_cleanup
