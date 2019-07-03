include:
  - bootstrap.remove_traditional_stack

{%- if salt['pillar.get']('contact_method') not in ['ssh-push', 'ssh-push-tunnel'] %}
mgr_salt_minion:
  pkg.installed:
    - name: salt-minion
    - order: last
  service.running:
    - name: salt-minion
    - enable: True
    - order: last
{% endif %}
