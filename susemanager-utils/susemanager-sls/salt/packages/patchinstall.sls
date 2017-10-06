{% if pillar.get('param_update_stack_patches', []) %}
mgr_update_stack_patches:
  pkg.patch_installed:
    - refresh: true
    - advisory_ids:
{%- for patch in pillar.get('param_update_stack_patches', []) %}
      - {{ patch }}
{%- endfor %}
    - diff_attr: ['epoch', 'version', 'release', 'arch', 'install_date_time_t']
    - require:
        - file: mgrchannels*
{% endif %}

{% if pillar.get('param_regular_patches', []) %}
mgr_regular_patches:
  pkg.patch_installed:
{% if not pillar.get('param_update_stack_patches', []) %}
    - refresh: true
{% endif %}
    - advisory_ids:
{%- for patch in pillar.get('param_regular_patches', []) %}
      - {{ patch }}
{%- endfor %}
    - diff_attr: ['epoch', 'version', 'release', 'arch', 'install_date_time_t']
    - require:
        - file: mgrchannels*
{% if pillar.get('param_update_stack_patches', []) %}
        - pkg: mgr_update_stack_patches
{% endif %}
{% endif %}

include:
  - channels
