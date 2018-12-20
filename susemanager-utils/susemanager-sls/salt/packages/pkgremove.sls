{% if pillar.get('param_pkgs') %}
pkg_removed:
  pkg.removed:
    -   pkgs:
{%- for pkg, arch, version in pillar.get('param_pkgs', []) %}
    {%- if grains['os_family'] == 'Debian' %}
        - {{ pkg }}:{{ arch }}: {{ version }}
    {%- elif grains.get('__suse_reserved_pkg_all_versions_support', False) %}
        - {{ pkg }}.{{ arch }}: {{ version }}
    {%- else %}
        - {{ pkg }}: {{ version }}
    {%- endif %}
{%- endfor %}
    -   require:
        - file: mgrchannels*
{% endif %}

include:
  - channels
