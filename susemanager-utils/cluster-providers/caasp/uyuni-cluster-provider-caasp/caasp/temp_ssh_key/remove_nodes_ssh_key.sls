remove_prev_temp_ssh_key:
  ssh_auth.absent:
    - user: root
    - source: salt://cluster/{{ pillar['cluster'] }}/temp_caasp_key.pub
