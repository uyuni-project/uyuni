{% if pillar.get('param_patches', []) %}
pkg_downloaded-patches:
  pkg.patch_downloaded:
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
