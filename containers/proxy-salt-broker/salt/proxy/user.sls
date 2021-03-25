cont_force_gid_squid:
  group.present:
    - name: squid
    - gid: 477

cont_force_gid_salt:
  group.present:
    - name: salt
    - gid: 476

cont_force_gid_mgrsshtunnel:
  group.present:
    - name: mgrsshtunnel
    - gid: 475

cont_force_uid_squid:
  user.present:
    - name: squid
    - uid: 477
    - gid: 477
    - allow_uid_change: True
    - allow_gid_change: True
    - home: /var/cache/squid
    - createhome: False
    - shell: /sbin/nologin
    - fullname: WWW-proxy squid

cont_force_uid_salt:
  user.present:
    - name: salt
    - uid: 476
    - gid: 476
    - allow_uid_change: True
    - allow_gid_change: True
    - home: /var/lib/salt
    - createhome: False
    - shell: /sbin/false
    - fullname: salt-master daemon

cont_force_uid_mgrsshtunnel:
  user.present:
    - name: mgrsshtunnel
    - uid: 475
    - gid: 475
    - allow_uid_change: True
    - allow_gid_change: True
    - home: /var/lib/spacewalk/mgrsshtunnel
    - createhome: False
    - shell: /sbin/bash
    - fullname: susemanager ssh push tunnel
