{% if pillar.get('param_patches', []) %}
pkg_downloaded-patches:
  pkg.patch_downloaded:
    - advisory_ids:
{%- for patch in pillar.get('param_patches', []) %}
      - {{ patch }}
{%- endfor %}
    - require:
      - mgrcompat: applychannels
{% endif %}

applychannels:
    mgrcompat.module_run:
    -  name: state.apply
    -  mods: channels
