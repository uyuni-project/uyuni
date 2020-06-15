{%- set ssh_agent_key = pillar['ssh_agent_key'] %}
{%- if ssh_agent_key %}
mgr_caasp_load_ssh_agent:
  module.run:
    - name: ssh_agent.start_agent

mgr_caasp_add_key:
  module.run:
    - name: ssh_agent.add_key
    - ssh_key_file: {{ ssh_agent_key }}
    - require:
      - module: mgr_caasp_load_ssh_agent
      
mgr_caasp_list_keys:
  module.run:
    - name: ssh_agent.list_keys
    - require:
      - module: mgr_caasp_add_key
{%- else %}
mgr_caasp_nop:
  test.nop
{%- endif %}
