mgr_ssh_identity:
  ssh_auth.present:
    - user: root
    - source: salt://salt_ssh/mgr_ssh_id.pub
{% if salt['pillar.get']('contact_method') == 'ssh-push-tunnel' %}
mgr_server_localhost_alias_present:
  host.present:
    - ip:
      - 127.0.0.1
    - names:
      - {{ salt['pillar.get']('mgr_server') }}
{% endif %}
