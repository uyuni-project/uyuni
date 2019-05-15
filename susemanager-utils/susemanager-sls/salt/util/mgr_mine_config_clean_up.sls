{%- if salt['pillar.get']('contact_method') not in ['ssh-push', 'ssh-push-tunnel'] %}
mgr_disable_mine:
  file.managed:
    - name: /etc/salt/minion.d/susemanager-mine.conf
    - contents: "mine_enabled: False"

mgr_salt_minion:
  service.running:
   - name: salt-minion
   - enable: True
   - order: last
   - watch:
     - file: mgr_disable_mine
{% endif %}
