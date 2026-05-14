#!jinja|yaml
# Tooling required by the rke2-proxy install.
#
# Repo packages: curl, openssl, openssh, helm.
# kubectl is symlinked from the RKE2-bundled binary in rke2.sls.

{%- if grains['os_family'] != 'Suse' %}
rke2_proxy_unsupported_os:
  test.fail_without_changes:
    - name: |
        rke2-proxy state currently only supports the SUSE family
        (got os_family={{ grains['os_family'] }}). Adjust packages.sls
        for your distribution.
{%- else %}

rke2_proxy_pkgs_installed:
  pkg.installed:
    - pkgs:
      - curl
      - openssl
      - openssh
      - helm
    - refresh: True

{%- endif %}
