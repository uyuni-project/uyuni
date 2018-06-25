{% if pillar.get('param_pkgs') %}
pkg_downloaded:
  pkg.downloaded:
    - pkgs:
{%- for pkg, arch, version in pillar.get('param_pkgs', []) %}
    {%- if grains.get('__suse_reserved_pkg_all_versions_support', False) %}
        - {{ pkg }}.{{ arch }}: {{ version }}
    {%- else %}
        - {{ pkg }}: {{ version }}
    {%- endif %}
{%- endfor %}
    - require:
        - file: mgrchannels*
{% endif %}

include:
  - channels
