{%- if not salt['pillar.get']('params:use_ssh_agent') %}
mgr_caasp_kill_agent:
  module.run:
    - name: ssh_agent.kill
    - order: last
    - require: 
      - module: mgr_caasp_add_key
{%- endif %}