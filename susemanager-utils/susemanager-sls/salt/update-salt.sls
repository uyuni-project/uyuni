include:
  - channels

{%- if salt['pillar.get']('contact_method') not in ['ssh-push', 'ssh-push-tunnel'] %}
mgr_keep_salt_up2date:
  pkg.latest:
    - refresh: True
    - pkgs:
{%- if salt['pkg.version']('salt-minion') %}
      - salt-minion
{%- if grains.os_family == 'Debian' %}
      - salt-common
{%- else %}
      - salt
      - python3-salt
{%- endif %}
{%- endif %}
{%- if salt['pkg.version']('venv-salt-minion') %}
      - venv-salt-minion
{%- endif %}
    - require:
      - sls: channels
{%- endif %}
