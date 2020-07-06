{%- set params = pillar['params'] %}
{%- set temp_ssh_key = params.ssh_key == 'temporary' %}
{%- if temp_ssh_key %}
include:
  - caasp.temp_ssh_key
  - caasp.temp_ssh_key.cleanup
{%- endif %}

mgr_remove_nodes:
  salt.state:
    - tgt: {{ pillar['management_node'] }}
    - sls:
      - clusters.removenode
    - pillar:
        params: {{ pillar['params'] }}
        cluster_type: caasp
{%- if temp_ssh_key %}
        state_hooks:
            list:
                before: [caasp.temp_ssh_key.init_ssh_agent]
                after: [caasp.temp_ssh_key.kill_ssh_agent]
        ssh_agent_key: /root/.ssh/temp_caasp_key
{%- endif %}
{%- if temp_ssh_key %}
    - require:
      - sls: caasp.temp_ssh_key
{%- endif %}
