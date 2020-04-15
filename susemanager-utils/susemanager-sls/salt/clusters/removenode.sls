{%- if pillar.get('ssh_auth_sock', False) %}
ssh_agent_socket:
  environ.setenv:
    - name: SSH_AUTH_SOCK
    - value: {{ pillar['ssh_auth_sock'] }}
{%- endif %}

mgr_cluster_remove_node:
  module.run:
    - name: mgrclusters.remove_node
    - provider_module: {{ pillar['cluster_type'] }}
    - params: {{ pillar['params'] }}
   {%- if pillar.get('ssh_auth_sock', False) %}
    - require:
      - environ: ssh_agent_socket
   {%- endif %}
