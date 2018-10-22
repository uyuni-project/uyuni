{% if pillar.get('param_pkgs') %}
pkg_installed:
  pkg.installed:
    -   refresh: true
    -   pkgs:
{%- for pkg, arch, version in pillar.get('param_pkgs', []) %}
    {%- if grains['os_family'] == 'Debian' %}
        - {{ pkg }}: {{ version }}
        - skip_verify: true
    {%- else %}
    {%- if grains.get('__suse_reserved_pkg_all_versions_support', False) %}
        - {{ pkg }}.{{ arch }}: {{ version }}
    {%- else %}
        - {{ pkg }}: {{ version }}
    {%- endif %}
    {%- endif %}

{%- endfor %}
    - diff_attr: ['epoch', 'version', 'release', 'arch', 'install_date_time_t']
    -   require:
        - file: mgrchannels*
{% endif %}

include:
  - channels
