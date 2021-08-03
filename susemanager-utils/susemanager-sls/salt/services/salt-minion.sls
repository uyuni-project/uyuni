{% include 'bootstrap/remove_traditional_stack.sls' %}
{%- set salt_minion_name = 'salt-minion' %}
{%- set susemanager_minion_config = '/etc/salt/minion.d/susemanager.conf' %}
{# Prefer venv-salt-minion if installed #}
{%- if salt['pkg.version']('venv-salt-minion') %}
{%- set salt_minion_name = 'venv-salt-minion' %}
{%- set susemanager_minion_config = '/etc/venv-salt-minion/minion.d/susemanager.conf' %}
{%- endif -%}

{%- if salt['pillar.get']('contact_method') not in ['ssh-push', 'ssh-push-tunnel'] %}

{# management keys should be used only once #}
{# removed to prevent trouble on the next regular minion restart #}
mgr_remove_management_key_grains:
  file.replace:
    - name: {{ susemanager_minion_config }}
    - pattern: '^\s*management_key:.*$'
    - repl: ''
    - onlyif: grep 'management_key:' {{ susemanager_minion_config }}

{# activation keys are only usefull on first registration #}
{# removed to prevent trouble on the next regular minion restart #}
mgr_remove_activation_key_grains:
  file.replace:
    - name: {{ susemanager_minion_config }}
    - pattern: '^\s*activation_key:.*$'
    - repl: ''
    - onlyif: grep 'activation_key:' {{ susemanager_minion_config }}

{# add SALT_RUNNING env variable in case it's not present on the configuration #}
mgr_append_salt_running_env_configuration:
  file.append:
    - name: {{ susemanager_minion_config }}
    - text: |
        system-environment:
          modules:
            pkg:
              _:
                SALT_RUNNING: 1
    - unless: grep 'system-environment' {{ susemanager_minion_config }}

mgr_salt_minion:
  pkg.installed:
    - name: {{ salt_minion_name }}
    - order: last
  service.running:
    - name: {{ salt_minion_name }}
    - enable: True
    - order: last
{% endif %}

{# ensure /etc/sysconfig/rhn/systemid is created to indicate minion is managed by SUSE Manager #}
/etc/sysconfig/rhn/systemid:
  file.managed:
    - mode: 0640
    - makedirs: True
    - replace: False
