include:
- channels
{% if pillar.get('param_appstreams_disable') %}
disable_appstreams:
  appstreams.disabled:
    - appstreams:
{%- for module_name in pillar.get('param_appstreams_disable', []) %}
      - {{ module_name }}
{%- endfor %}
{%- endif %}

{% if pillar.get('param_appstreams_enable') %}
enable_appstreams:
  appstreams.enabled:
    - appstreams:
{%- for module_name, stream in pillar.get('param_appstreams_enable', []) %}
      - {{ module_name }}:{{ stream }}
{%- endfor %}
    - require:
      - file: /etc/yum.repos.d/susemanager:channels.repo
    {% if pillar.get('param_appstreams_disable') %}
      - appstreams: disable_appstreams
    {%- endif %}
{%- endif %}

enabled_appstreams:
  mgrcompat.module_run:
    - name: appstreams.get_enabled_modules
    {% if pillar.get('param_appstreams_enable') %}
    - require:
      - appstreams: enable_appstreams
    {% elif pillar.get('param_appstreams_disable') %}
    - require:
      - appstreams: disable_appstreams
    {%- endif %}
