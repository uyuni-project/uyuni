##
##  java bootstrapping calls certs.sls before this state
##

mgr_ssh_identity:
  ssh_auth.present:
    - user: {{ salt['pillar.get']('mgr_sudo_user') or 'root' }}
    - source: salt://salt_ssh/mgr_ssh_id.pub
{% if salt['pillar.get']('contact_method') == 'ssh-push-tunnel' %}
mgr_server_localhost_alias_present:
  host.present:
{% else %}
mgr_server_localhost_alias_absent:
  host.absent:
{% endif %}
    - ip:
      - 127.0.0.1
    - names:
      - {{ salt['pillar.get']('mgr_server') }}

{%- if salt['pillar.get']('proxy_pub_key') and salt['pillar.get']('contact_method') == 'ssh-push-tunnel' %}
no_push_key_authorized:
  ssh_auth.absent:
    - user: {{ salt['pillar.get']('mgr_sudo_user') or 'root' }}
    - comment: susemanager-ssh-push

proxy_ssh_identity:
  ssh_auth.present:
    - user: {{ salt['pillar.get']('mgr_sudo_user') or 'root' }}
    - source: salt://salt_ssh/{{ salt['pillar.get']('proxy_pub_key') }}
    - require:
      - ssh_auth: no_push_key_authorized
{%- endif %}

{%- if salt['pillar.get']('mgr_sudo_user') and salt['pillar.get']('mgr_sudo_user') != 'root' %}
{%- set home = '/home/' ~ salt['pillar.get']('mgr_sudo_user') %}
{%- else %}
{%- set home = '/root' %}
{%- endif %}

generate_own_ssh_key:
  cmd.run:
    - name: ssh-keygen -N '' -C 'susemanager-own-ssh-push' -f {{ home }}/.ssh/mgr_own_id -t rsa -q
    - creates: {{ home }}/.ssh/mgr_own_id.pub

ownership_own_ssh_key:
  file.managed:
    - name: {{ home }}/.ssh/mgr_own_id
    - user: {{ salt['pillar.get']('mgr_sudo_user') or 'root' }}
    - require:
      - cmd: generate_own_ssh_key

no_own_key_authorized:
  ssh_auth.absent:
    - user: {{ salt['pillar.get']('mgr_sudo_user') or 'root' }}
    - comment: susemanager-own-ssh-push
    - require:
      - file: ownership_own_ssh_key

authorize_own_key:
  ssh_auth.present:
    - user: {{ salt['pillar.get']('mgr_sudo_user') or 'root' }}
    - source: {{ home }}/.ssh/mgr_own_id.pub
    - require:
      - file: ownership_own_ssh_key
      - ssh_auth: no_own_key_authorized

{% include 'channels/gpg-keys.sls' %}
{% include 'bootstrap/remove_traditional_stack.sls' %}
