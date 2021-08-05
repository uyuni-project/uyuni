{% include 'bootstrap/remove_traditional_stack.sls' %}

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

{# add SALT_RUNNING env variable in case it's not present on the configuration #}
mgr_append_salt_running_env_configuration:
  file.append:
    - name: /etc/salt/minion.d/susemanager.conf
    - text: |
        system-environment:
          modules:
            pkg:
              _:
                SALT_RUNNING: 1
    - unless: grep 'system-environment' /etc/salt/minion.d/susemanager.conf

mgr_salt_minion:
  pkg.installed:
    - name: salt-minion
    - order: last
  service.running:
    - name: salt-minion
    - enable: True
    - order: last
{% endif %}

{%- if salt['pillar.get']('contact_method') in ['ssh-push', 'ssh-push-tunnel'] %}
logrotate_configuration:
  file.managed:
    - name: /etc/logrotate.d/salt-ssh
    - user: root
    - group: root
    - mode: 644
    - makedirs: True
    - contents: |
        /var/log/salt-ssh.log {
                su root root
                weekly
                missingok
                rotate 7
                compress
                notifempty
        }
{% endif %}

{# ensure /etc/sysconfig/rhn/systemid is created to indicate minion is managed by SUSE Manager #}
/etc/sysconfig/rhn/systemid:
  file.managed:
    - mode: 0640
    - makedirs: True
    - replace: False
