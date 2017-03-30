{% if pillar.get('patches', []) %}
pkg_downloaded-patches:
  module.run:
    - name: pkg.install
    - patches:
{%- for patch in pillar.get('patches', []) %}
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
