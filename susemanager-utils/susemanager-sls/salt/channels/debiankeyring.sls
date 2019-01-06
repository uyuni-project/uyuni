{%- if pillar.get('mgr_metadata_signing_enabled', false) %}
mgr_debian_repo_keyring:
  file.managed:
    - name: /usr/share/keyrings/mgr-archive-keyring.gpg
    - source: salt://gpg/mgr-keyring.gpg
    - mode: 644
{%- endif %}
