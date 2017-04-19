{% if pillar.get('param_patches', []) %}
patchinstall:
  pkg.patch_installed:
    - refresh: true
    - advisory_ids:
{%- for patch in pillar.get('param_patches', []) %}
      - {{ patch }}
{%- endfor %}
    - require:
        - file: mgrchannels*
{% endif %}

include:
  - channels
