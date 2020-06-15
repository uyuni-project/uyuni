mgr_caasp_kill_agent:
  module.run:
    - name: ssh_agent.kill
    - order: last
