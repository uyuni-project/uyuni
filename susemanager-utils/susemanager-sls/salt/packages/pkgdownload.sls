{% if pillar.get('param_pkgs', {}).items() %}
pkg_downloaded:
  pkg.downloaded:
    - pkgs:
{%- for pkg, version in pillar.get('param_pkgs', {}).items() %}
      - {{ pkg }}-{{ version }}
{%- endfor %}
    - require:
      - module: applychannels
{% endif %}

applychannels:
    module.run:
    -  name: state.apply
    -  mods: channels
