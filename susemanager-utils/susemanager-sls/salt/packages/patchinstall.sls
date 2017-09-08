{% if pillar.get('param_patches', []) %}
patchinstall:
  pkg.patch_installed:
    - refresh: true
    - advisory_ids:
{%- for patch in pillar.get('param_patches', []) %}
      - {{ patch }}
{%- endfor %}
    - diff_attr: ['epoch', 'version', 'release', 'arch', 'install_date_time_t']
    - require:
        - file: mgrchannels*
{% endif %}

include:
  - channels
