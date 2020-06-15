remove_prev_temp_key:
  ssh_auth.absent:
    - user: root
    - comment: temp-caasp-key

caasp_temp_key_present:
  ssh_auth.present:
    - user: root
    - source: salt://cluster/{{ pillar['cluster'] }}/temp_caasp_key.pub
    - config: '%h/.ssh/authorized_keys'
    - require:
      - ssh_auth: remove_prev_temp_key
