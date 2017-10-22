{% if salt['pillar.get']('contact_method') == 'ssh-push-tunnel' %}
# remove server to localhost aliasing from /etc/hosts
mgr_remove_mgr_server_localhost_alias:
  host.absent:
    - ip:
      - 127.0.0.1
    - names:
      - {{ salt['pillar.get']('mgr_server') }}
{%- endif %}

# remove server ssh authorization
mgr_remove_mgr_ssh_identity:
  ssh_auth.absent:
    - user: {{ salt['pillar.get']('mgr_sudo_user') or 'root' }}
    - source: salt://salt_ssh/mgr_ssh_id.pub

# remove proxy ssh authorization (if any)
{%- if salt['pillar.get']('mgr_server') != salt['pillar.get']('contact_method') == 'ssh-push-tunnel' %}
mgr_remove_proxy_ssh_identity:
  ssh_auth.absent:
    - user: {{ salt['pillar.get']('mgr_sudo_user') or 'root' }}
    - source: salt://salt_ssh/{{ salt['pillar.get']('proxy_pub_key') }}
{%- endif %}

{%- if salt['pillar.get']('mgr_sudo_user') and salt['pillar.get']('mgr_sudo_user') != 'root' %}
{%- set home = '/home/' ~ salt['pillar.get']('mgr_sudo_user') %}
{%- else %}
{%- set home = '/root' %}
{%- endif %}

# remove own key authorization
mgr_no_own_key_authorized:
  ssh_auth.absent:
    - user: {{ salt['pillar.get']('mgr_sudo_user') or 'root' }}
    - source: {{ home }}/.ssh/mgr_own_id.pub

# remove own keys
mgr_remove_own_ssh_pub_key:
  file.absent:
    - name: {{ home }}/.ssh/mgr_own_id.pub
    - require:
      - ssh_auth: mgr_no_own_key_authorized

mgr_remove_own_ssh_key:
  file.absent:
    - name: {{ home }}/.ssh/mgr_own_id
