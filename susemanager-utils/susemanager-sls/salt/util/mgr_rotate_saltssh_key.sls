{% set mgr_sudo_user = salt['pillar.get']('mgr_sudo_user') or 'root' %}

{% if salt['cp.list_master'](prefix='salt_ssh/new_mgr_ssh_id.pub') %}
{% if salt['pillar.get']('contact_method') in ['ssh-push', 'ssh-push-tunnel'] %}
new_mgr_ssh_identity:
  ssh_auth.present:
    - user: {{ mgr_sudo_user }}
    - source: salt://salt_ssh/new_mgr_ssh_id.pub
{% endif %}

proxy_new_mgr_ssh_identity:
  ssh_auth.present:
    - user: mgrsshtunnel
    - source: salt://salt_ssh/new_mgr_ssh_id.pub
    - onlyif: grep -q mgrsshtunnel /etc/passwd
{% endif %}

{% if salt['cp.list_master'](prefix='salt_ssh/disabled_mgr_ssh_id.pub') %}
{% if salt['pillar.get']('contact_method') in ['ssh-push', 'ssh-push-tunnel'] %}
old_mgr_ssh_identity:
  ssh_auth.absent:
    - user: {{ mgr_sudo_user }}
    - source: salt://salt_ssh/disabled_mgr_ssh_id.pub

# to prevent to lock out yourself
current_mgr_ssh_identity:
  ssh_auth.present:
    - user: {{ mgr_sudo_user }}
    - source: salt://salt_ssh/mgr_ssh_id.pub
{% endif %}

proxy_old_mgr_ssh_identity:
  ssh_auth.absent:
    - user: mgrsshtunnel
    - source: salt://salt_ssh/disabled_mgr_ssh_id.pub
    - onlyif: grep -q mgrsshtunnel /etc/passwd

# to prevent to lock out yourself
proxy_current_mgr_ssh_identity:
  ssh_auth.present:
    - user: mgrsshtunnel
    - source: salt://salt_ssh/mgr_ssh_id.pub
    - onlyif: grep -q mgrsshtunnel /etc/passwd
{% endif %}
