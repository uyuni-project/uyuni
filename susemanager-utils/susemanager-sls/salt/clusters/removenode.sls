{%- if pillar['params'].get('ssh_auth_sock', False) %}
mgr_ssh_agent_socket_clusters_removenode:
  environ.setenv:
    - name: SSH_AUTH_SOCK
    - value: {{ pillar['params'].get('ssh_auth_sock') }}
{%- endif %}

{%- set params = pillar['params'] %}
{%- for node in params.nodes %}
{%- set removeparams = {'node_name': node.node_name, 'skuba_cluster_path': params.skuba_cluster_path, 'drain_timeout': params.drain_timeout } %}
mgr_cluster_remove_node_{{ node.node_name }}:
  module.run:
    - name: mgrclusters.remove_node
    - provider_module: {{ pillar['cluster_type'] }}
    - params: {{ removeparams }}
    - require:
   {%- if grains.get('__suse_reserved_saltutil_states_support', False) %}
      - saltutil: sync_modules
   {%- else %}
      - module: sync_modules
   {%- endif %}
   {%- if pillar.get('ssh_auth_sock', False) %}
      - environ: mgr_ssh_agent_socket_clusters_removenode
   {%- endif %}
{%- for hook in pillar['state_hooks'].get('remove', {}).get('before', []) %}
      - sls: {{ hook }}
{%- endfor %}   
{%- endfor %}

include:
  - util.syncmodules
{%- for hook in pillar['state_hooks'].get('remove', {}).get('before', []) %}
  - {{ hook }}
{%- endfor %}
{%- for hook in pillar['state_hooks'].get('remove', {}).get('after', []) %}
  - {{ hook }}
{%- endfor %}