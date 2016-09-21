mgr_ssh_identity:
  ssh_auth.present:
    - user: root
    - source: salt://salt_ssh/mgr_ssh_id.pub
