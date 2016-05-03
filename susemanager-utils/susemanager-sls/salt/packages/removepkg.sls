{% if pillar.get('pkgs', {}).items() %}
pkg_removed:
  pkg.removed:
    -   pkgs:
{%- for pkg, version in pillar.get('pkgs', {}).items() %}
        - {{ pkg }}: {{ version }}
{%- endfor %}
    -   require:
        - module: applychannels
{% endif %}

applychannels:
    module.run:
    -  name: state.apply
    -  mods: channels