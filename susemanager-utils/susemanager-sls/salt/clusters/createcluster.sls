{%- if pillar.get('ssh_auth_sock', False) %}
mgr_ssh_agent_socket_clusters_createcluster:
  environ.setenv:
    - name: SSH_AUTH_SOCK
    - value: {{ pillar['ssh_auth_sock'] }}
{%- endif %}

mgr_cluster_create_cluster:
  module.run:
    - name: mgrclusters.create_cluster
    - provider_module: {{ pillar['cluster_type'] }}
    - params: {{ pillar['params'] }}
    - require:
   {%- if grains.get('__suse_reserved_saltutil_states_support', False) %}
      - saltutil: sync_modules
   {%- else %}
      - module: sync_modules
   {%- endif %}
   {%- if pillar.get('ssh_auth_sock', False) %}
      - environ: mgr_ssh_agent_socket_clusters_createcluster
   {%- endif %}

include:
  - util.syncmodules
