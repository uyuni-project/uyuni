{% if pillar.get('pkgs', {}).items() %}
pkg_installed:
  pkg.installed:
    -   refresh: true
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