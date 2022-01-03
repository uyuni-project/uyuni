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
