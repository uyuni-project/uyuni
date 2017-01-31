include:
  - bootstrap.remove_traditional_stack

{%- if salt['pillar.get']('contact_method') not in ['ssh-push', 'ssh-push-tunnel'] %}
mgr_mine_config:
  file.managed:
    - name: /etc/salt/minion.d/susemanager-mine.conf
    - contents: |
        mine_return_job: True

salt-minion:
  pkg.installed:
    - name: salt-minion
    - order: last
  service.running:
    - enable: True
    - order: last
    - watch:
      - file: mgr_mine_config
{% endif %}
