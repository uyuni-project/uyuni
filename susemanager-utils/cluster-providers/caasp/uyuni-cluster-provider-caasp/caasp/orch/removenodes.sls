{%- set params = pillar['params'] %}
{%- if params.temp_ssh_key %}
include:
  - caasp.temp_ssh_key
  - caasp.temp_ssh_key.cleanup
{%- endif %}

mgr_remote_nodes:
  salt.state:
    - tgt: {{ pillar['management_node'] }}
    - sls:
      - clusters.removenode
    - pillar:
        params: {{ pillar['params'] }}
        cluster_type: caasp
{%- if params.temp_ssh_key %}            
        state_hooks:
            list:
                before: [caasp.temp_ssh_key.init_ssh_agent]
                after: [caasp.temp_ssh_key.kill_ssh_agent]
        ssh_agent_key: /root/.ssh/temp_caasp_key
{%- endif %}
{%- if params.temp_ssh_key %}      
    - require:
      - sls: caasp.temp_ssh_key
{%- endif %}
