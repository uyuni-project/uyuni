{%- if pillar.get('ssh_auth_sock', False) %}
mgr_ssh_agent_socket_clusters_removenode:
  environ.setenv:
    - name: SSH_AUTH_SOCK
    - value: {{ pillar['ssh_auth_sock'] }}
{%- endif %}

mgr_cluster_remove_node:
  module.run:
    - name: mgrclusters.remove_node
    - provider_module: {{ pillar['cluster_type'] }}
    - params: {{ pillar['params'] }}
    - require:
   {%- if grains.get('__suse_reserved_saltutil_states_support', False) %}
      - saltutil: sync_modules
   {%- else %}
      - module: sync_modules
   {%- endif %}
   {%- if pillar.get('ssh_auth_sock', False) %}
      - environ: mgr_ssh_agent_socket_clusters_removenode
   {%- endif %}

include:
  - util.syncmodules
