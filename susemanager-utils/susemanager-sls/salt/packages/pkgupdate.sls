include:
  - channels

mgr_pkg_update:
  pkg.uptodate:
{%- if grains['os_family'] == 'Debian' %}
    - skip_verify: {{ not pillar.get('mgr_metadata_signing_enabled', false) }}
{%- endif %}
    - diff_attr: ['epoch', 'version', 'release', 'arch', 'install_date_time_t']
    - require:
      - file: mgrchannels*
