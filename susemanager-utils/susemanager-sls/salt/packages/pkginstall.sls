{% if pillar.get('param_pkgs') %}
pkg_installed:
  pkg.installed:
    -   refresh: true
{%- if grains['os_family'] == 'Debian' %}
    - skip_verify: {{ not pillar.get('mgr_metadata_signing_enabled', false) }}
{%- endif %}
    -   pkgs:
{%- for pkg, arch, version in pillar.get('param_pkgs', []) %}
    {%- if grains.get('__suse_reserved_pkg_all_versions_support', False) %}
        - {{ pkg }}.{{ arch }}: {{ version }}
    {%- else %}
        - {{ pkg }}: {{ version }}
    {%- endif %}

{%- endfor %}
    - diff_attr: ['epoch', 'version', 'release', 'arch', 'install_date_time_t']
    -   require:
        - file: mgrchannels*
{% endif %}

include:
  - channels
