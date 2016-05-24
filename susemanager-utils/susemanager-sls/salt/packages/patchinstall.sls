{% if pillar.get('param_pkgs', {}).items() %}
patchinstall:
  module.run:
    - name: pkg.install
    - refresh: true
    - pkgs:
{%- for pkg, version in pillar.get('param_pkgs', {}).items() %}
      - {{ pkg }}: {{ version }}
{%- endfor %}
    - require:
      - module: applychannels
{% endif %}

applychannels:
  module.run:
    -  name: state.apply
    -  mods: channels
