mgr_ssh_identity:
  ssh_auth.present:
    - user: root
    - source: salt://mgr_ssh_id.pub
