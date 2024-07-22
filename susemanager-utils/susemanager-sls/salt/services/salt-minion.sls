{% include 'bootstrap/remove_traditional_stack.sls' %}
{%- set salt_minion_name = 'salt-minion' %}
{%- set susemanager_minion_config = '/etc/salt/minion.d/susemanager.conf' %}
{# Prefer venv-salt-minion if installed #}
{%- if salt['pkg.version']('venv-salt-minion') %}
{%- set salt_minion_name = 'venv-salt-minion' %}
{%- set susemanager_minion_config = '/etc/venv-salt-minion/minion.d/susemanager.conf' %}
{%- endif -%}

{%- if salt['pillar.get']('contact_method') not in ['ssh-push', 'ssh-push-tunnel'] %}

mgr_salt_minion_inst:
  pkg.installed:
    - name: {{ salt_minion_name }}
    - order: last

{{ susemanager_minion_config }}:
  file.managed:
    - source:
      - salt://bootstrap/susemanager.conf
    - template: jinja
    - mode: 644
    - order: last
    - require:
      - pkg: mgr_salt_minion_inst

{%- if salt_minion_name == 'venv-salt-minion' %}
rm_old_venv_python_env:
  cmd.run:
    - name: /usr/lib/venv-salt-minion/bin/post_start_cleanup.sh
    - onlyif: test -f /usr/lib/venv-salt-minion/bin/post_start_cleanup.sh
{%- endif %}

mgr_salt_minion_run:
  service.running:
    - name: {{ salt_minion_name }}
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
                missingok
                size 10M
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
