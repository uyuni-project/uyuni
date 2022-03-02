mgr_disable_fqdns_grains:
  file.append:
    - name: /etc/salt/minion.d/susemanager.conf
    - text: "enable_fqdns_grains: False"
    - unless: grep 'enable_fqdns_grains:' /etc/salt/minion.d/susemanager.conf

mgr_salt_minion:
  service.running:
   - name: salt-minion
   - enable: True
   - order: last
   - watch:
     - file: mgr_disable_fqdns_grains
