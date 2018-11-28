{%- if pillar.get('_mgr_metadata_signing_enabled', false) %}
/usr/share/keyrings/mgr-archive-keyring.gpg:
  file.managed:
    - source:
      - salt://gpg/mgr-keyring.gpg
    - mode: 644
{%- endif %}