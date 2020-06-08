{%- if pillar['params'].get('ssh_auth_sock', False) %}
mgr_ssh_agent_socket_clusters_addnode:
  environ.setenv:
    - name: SSH_AUTH_SOCK
    - value: {{ pillar['params'].get('ssh_auth_sock') }}
{%- endif %}

{%- set params = pillar['params'] %}
{%- for node in params.nodes %}
{%- set addparams = {'node_name': node.node_name, 'target': node.target, 'role': params.role, 'user': params.user, 'skuba_cluster_path': params.skuba_cluster_path } %}
mgr_cluster_add_node_{{ node.node_name }}:
  module.run:
    - name: mgrclusters.add_node
    - provider_module: {{ pillar['cluster_type'] }}
    - params: {{ addparams }}
    - require:
   {%- if grains.get('__suse_reserved_saltutil_states_support', False) %}
      - saltutil: sync_modules
   {%- else %}
      - module: sync_modules
   {%- endif %}
   {%- if pillar['params'].get('ssh_auth_sock', False) %}
      - environ: mgr_ssh_agent_socket_clusters_addnode
   {%- endif %}
{%- for hook in pillar['state_hooks'].get('join', {}).get('before', []) %}
      - sls: {{ hook }}
{%- endfor %}
{%- endfor %}

include:
  - util.syncmodules
{%- for hook in pillar['state_hooks'].get('join', {}).get('before', []) %}
  - {{ hook }}
{%- endfor %}
{%- for hook in pillar['state_hooks'].get('join', {}).get('after', [])%}
  - {{ hook }}
{%- endfor %}
