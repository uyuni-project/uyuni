{% if pillar.get('param_pkgs', {}).items() %}
pkg_downloaded:
  module.run:
    - name: pkg.install
    - pkgs:
{%- for pkg, version in pillar.get('param_pkgs', {}).items() %}
      - {{ pkg }}-{{ version }}
{%- endfor %}
    - downloadonly: True
    - require:
      - module: applychannels
{% endif %}

applychannels:
    module.run:
    -  name: state.apply
    -  mods: channels
