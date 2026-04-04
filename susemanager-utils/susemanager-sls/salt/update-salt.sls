include:
  - channels

{%- if salt['pillar.get']('contact_method') not in ['ssh-push', 'ssh-push-tunnel'] %}
mgr_keep_salt_up2date:
  pkg.latest:
    - refresh: True
    - pkgs:
{%- if '/venv-salt-minion/' in grains['pythonexecutable'] %}
      - venv-salt-minion
{%- else %}
      - salt-minion
{%- if grains.os_family == 'Debian' %}
      - salt-common
{%- else %}
      - salt
      {%- if grains['os_family'] == "Suse" and grains['osrelease'] == '15.7' %}
      - python311-salt
      {%- else %}
      - python3-salt
      {%- endif %}
{%- endif %}
{%- endif %}
    - require:
      - sls: channels
{%- endif %}
