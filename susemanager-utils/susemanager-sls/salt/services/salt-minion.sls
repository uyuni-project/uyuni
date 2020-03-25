include:
  - bootstrap.remove_traditional_stack

{%- if salt['pillar.get']('contact_method') not in ['ssh-push', 'ssh-push-tunnel'] %}

{# management keys should be used only once #}
{# removed to prevent trouble on the next regular minion restart #}
mgr_remove_management_key_grains:
  file.replace:
    - name: /etc/salt/minion.d/susemanager.conf
    - pattern: '^\s*management_key:.*$'
    - repl: ''
    - onlyif: grep 'management_key:' /etc/salt/minion.d/susemanager.conf

{# activation keys are only usefull on first registration #}
{# removed to prevent trouble on the next regular minion restart #}
mgr_remove_activation_key_grains:
  file.replace:
    - name: /etc/salt/minion.d/susemanager.conf
    - pattern: '^\s*activation_key:.*$'
    - repl: ''
    - onlyif: grep 'activation_key:' /etc/salt/minion.d/susemanager.conf

mgr_salt_minion:
  pkg.installed:
    - name: salt-minion
    - order: last
  service.running:
    - name: salt-minion
    - enable: True
    - order: last
{% endif %}
