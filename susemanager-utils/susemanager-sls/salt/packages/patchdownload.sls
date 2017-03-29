{% if pillar.get('patches', []) %}
pkg_downloaded-patches:
  pkg.downloaded:
    - patches:
{%- for patch in pillar.get('patches', []) %}
      - {{ patch }}
{%- endfor %}
    - require:
      - module: applychannels
{% endif %}

applychannels:
    module.run:
    -  name: state.apply
    -  mods: channels
