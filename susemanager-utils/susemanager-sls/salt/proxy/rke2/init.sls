#!jinja|yaml
# Install/configure SUSE Multi-Linux Manager 5.2 proxy on RKE2.
#
# Apply with:  salt '<minion>' state.apply rke2-proxy
#
# proxy_fqdn and server_fqdn are derived from the SUMA-managed pillar
# (primary_fqdn and mgr_server) if not set explicitly in rke2_proxy.

# --- Hardware validation ---------------------------------------------------

{%- set cpu_flags = salt['grains.get']('cpu_flags', []) %}
{%- if 'sse4_2' not in cpu_flags %}
proxy_rke2_cpu_check:
  test.fail_without_changes:
    - name: |
        CPU does not support x86-64-v2 (missing sse4_2).
        RKE2 container images require x86-64-v2.
{%- else %}
# --- Pillar validation: fail early if required keys are missing -------------

{%- set required_mlm = ['chart_oci', 'chart_version', 'image_repo', 'image_tag'] %}
{%- set missing = [] %}
{%- for k in required_mlm if not salt['pillar.get']('proxy:rke2:' ~ k) %}
  {%- do missing.append('proxy:rke2:' ~ k) %}
{%- endfor %}
{%- if not (salt['pillar.get']('proxy:rke2:proxy_fqdn') or salt['pillar.get']('primary_fqdn')) %}
  {%- do missing.append('proxy:rke2:proxy_fqdn (or SUMA primary_fqdn)') %}
{%- endif %}
{%- if not (salt['pillar.get']('proxy:rke2:server_fqdn') or salt['pillar.get']('mgr_server')) %}
  {%- do missing.append('proxy:rke2:server_fqdn (or SUMA mgr_server)') %}
{%- endif %}

{%- if missing %}
proxy_rke2_missing_pillar:
  test.fail_without_changes:
    - name: |
        Required pillar keys are missing or empty:
        {{ missing | join(', ') }}

        Set them in your pillar (see pillar.example.sls).
{%- else %}

include:
  - .packages
  - .rke2
  - .storage
  - .proxy_config
  - .chart

{%- endif %}
{%- endif %}
