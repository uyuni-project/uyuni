cont_force_gid_mgrsshtunnel:
  group.present:
    - name: mgrsshtunnel
    - gid: 474

cont_force_uid_mgrsshtunnel:
  user.present:
    - name: mgrsshtunnel
    - uid: 474
    - gid: 474
    - allow_uid_change: True
    - allow_gid_change: True
    - home: /var/lib/spacewalk/mgrsshtunnel
    - createhome: False
    - shell: /sbin/bash
    - fullname: susemanager ssh push tunnel
