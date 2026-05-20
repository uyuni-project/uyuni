# Example pillar for proxy.rke2 state.
# Copy to your pillar tree (e.g. /srv/pillar/proxy-rke2.sls) and adjust.
#
# proxy_fqdn and server_fqdn are optional if the minion is registered
# with SUMA — they default to primary_fqdn and mgr_server from the
# SUMA-managed pillar.
#
# Proxy config files (config.yaml, httpd.yaml, ssh.yaml) are backed up
# automatically from the running environment before wipe.

proxy:
  rke2:
    # Optional — derived from SUMA pillar if omitted
    # proxy_fqdn: mb-proxy52.mb-network-pub.lab
    # server_fqdn: mb-server.mb-network-pub.lab

    # Namespace for the proxy deployment (must match the existing k3s/RKE2 namespace)
    namespace: uyuni-proxy

    # Chart + images (required)
    chart_oci: oci://registry.suse.de/suse/sle-15-sp7/update/products/multilinuxmanager52/charts/suse/multi-linux-manager/5.2/proxy-helm
    chart_version: 5.2.0-beta2
    image_repo: registry.suse.de/devel/galaxy/manager/main/mlm-beta-products-sle15/containerfile/suse/multi-linux-manager/5.2/x86_64
    image_tag: latest

    # Directory with existing config.yaml/httpd.yaml/ssh.yaml.
    # If missing, config is extracted from the running k8s pod before wipe.
    # config_dir: /etc/uyuni/proxy

    # Toggles
    wipe_k8s: false                 # wipes both k3s and RKE2 if present
    enable_tftp_hostnetwork: true
