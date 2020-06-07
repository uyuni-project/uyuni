{%- if pillar['params'].get('ssh_auth_sock', False) %}
mgr_ssh_agent_socket_upgradecluster:
  environ.setenv:
    - name: SSH_AUTH_SOCK
    - value: {{ pillar['params'].get('ssh_auth_sock') }}
{%- endif %}

{%- set params = pillar['params'] %}
mgr_cluster_upgrade_cluster:
  module.run:
    - name: mgrclusters.upgrade_cluster
    - provider_module: {{ pillar['cluster_type'] }}
    - params: {{ params }}
    - require:
   {%- if grains.get('__suse_reserved_saltutil_states_support', False) %}
      - saltutil: sync_modules
   {%- else %}
      - module: sync_modules
   {%- endif %}
   {%- if pillar.get('ssh_auth_sock', False) %}
      - environ: mgr_ssh_agent_socket_upgradecluster
   {%- endif %}
{%- for hook in pillar['state_hooks'].get('remove', {}).get('before', []) %}
      - sls: {{ hook }}
{%- endfor %}    

include:
  - util.syncmodules
{%- for hook in pillar['state_hooks'].get('upgrade', {}).get('before', []) %}
  - {{ hook }}
{%- endfor %}
{%- for hook in pillar['state_hooks'].get('upgrade', {}).get('after', []) %}
  - {{ hook }}
{%- endfor %}
