{% include 'bootstrap/remove_traditional_stack.sls' %}

{%- if salt['pillar.get']('contact_method') not in ['ssh-push', 'ssh-push-tunnel'] %}

mgr_salt_minion_inst:
  pkg.installed:
    - name: salt-minion
    - order: last

/etc/salt/minion.d/susemanager.conf:
  file.managed:
    - source:
      - salt://bootstrap/susemanager.conf
    - template: jinja
    - mode: 644
    - order: last
    - require:
      - pkg: mgr_salt_minion_inst

mgr_salt_minion_run:
  service.running:
    - name: salt-minion
    - enable: True
    - order: last

{% endif %}
