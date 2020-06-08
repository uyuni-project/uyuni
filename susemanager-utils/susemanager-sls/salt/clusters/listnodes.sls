{%- if pillar['params'].get('ssh_auth_sock', False) %}
mgr_ssh_agent_socket_clusters_listnodes:
  environ.setenv:
    - name: SSH_AUTH_SOCK
    - value: {{ pillar['params'].get('ssh_auth_sock') }}
{%- endif %}

mgr_cluster_list_nodes:
  module.run:
    - name: mgrclusters.list_nodes
    - provider_module: {{ pillar['cluster_type'] }}
    - params: {{ pillar['params'] }}
    - require:
   {%- if grains.get('__suse_reserved_saltutil_states_support', False) %}
      - saltutil: sync_modules
   {%- else %}
      - module: sync_modules
   {%- endif %}
   {%- if pillar['params'].get('ssh_auth_sock', False) %}
      - environ: mgr_ssh_agent_socket_clusters_listnodes
   {%- endif %}

include:
  - util.syncmodules
