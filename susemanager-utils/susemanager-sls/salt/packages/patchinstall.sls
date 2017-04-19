{% if pillar.get('param_pkgs', {}).items() %}
patchinstall:
  pkg.installed:
    - refresh: true
    - pkgs:
{%- for pkg, version in pillar.get('param_pkgs', {}).items() %}
      - {{ pkg }}: {{ version }}
{%- endfor %}
    - require:
      - module: applychannels
{% endif %}

{% if pillar.get('param_patches', []) %}
patchinstall:
  pkg.patch_installed:
    - refresh: true
    - advisory_ids:
{%- for patch in pillar.get('param_patches', []) %}
      - {{ patch }}
{%- endfor %}
    - require:
      - module: applychannels
{% endif %}

applychannels:
  module.run:
    -  name: state.apply
    -  mods: channels
