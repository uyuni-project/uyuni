mgr_disable_fqdns_grains:
  file.append:
    - name: /etc/salt/minion.d/susemanager.conf
    - text: "enable_fqdns_grains: False"

mgr_salt_minion:
  service.running:
   - name: salt-minion
   - enable: True
   - order: last
   - watch:
     - file: mgr_disable_fqdns_grains
