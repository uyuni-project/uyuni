{% if pillar.get('param_patches', []) %}
pkg_downloaded-patches:
  module.run:
    - name: pkg.install
    - patches:
{%- for patch in pillar.get('param_patches', []) %}
      - {{ patch }}
{%- endfor %}
    - downloadonly: true
    - require:
      - module: applychannels
{% endif %}

applychannels:
    module.run:
    -  name: state.apply
    -  mods: channels
