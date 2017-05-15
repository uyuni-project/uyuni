{% if pillar.get('param_pkgs', {}).items() %}
pkg_removed:
  pkg.removed:
    -   pkgs:
{%- for pkg, version in pillar.get('param_pkgs', {}).items() %}
        - {{ pkg }}: {{ version }}
{%- endfor %}
    -   require:
        - file: mgrchannels*
{% endif %}

include:
  - channels
