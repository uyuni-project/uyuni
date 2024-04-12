disable_appstreams:
  appstreams.disabled:
    - appstreams:
{%- for module_name in pillar.get('param_appstreams_disable', []) %}
      - {{ module_name }}
{%- endfor %}

enable_appstreams:
  appstreams.enabled:
    - appstreams:
{%- for module_name, stream in pillar.get('param_appstreams_enable', []) %}
      - {{ module_name }}:{{ stream }}
{%- endfor %}
    - require:
      - appstreams: disable_appstreams

enabled_appstreams:
  mgrcompat.module_run:
    - name: appstreams.get_enabled_modules
    - require:
      - appstreams: enable_appstreams
