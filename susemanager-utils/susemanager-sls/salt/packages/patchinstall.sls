{% if grains.get('saltversioninfo', []) < [2015, 8, 12] %}
{{ salt.test.exception("You are running an old version of salt-minion that does not support patching. Please update salt-minion and try again.") }}
{% endif %}

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
    - novendorchange:  {{ not pillar.get('allow_vendor_change', False) }}
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
