{% set active = salt.virt.network_info(pillar['network_name']).get(pillar['network_name'], {}).get('active') %}
{% set state = 'running' if active else pillar['action_type'] %}
{% macro optional(name) -%}
  {%- if pillar[name]|default(none) %}
    - {{ name }}: {{ pillar[name] }}
  {%- endif %}
{%- endmacro %}

{% set  optional_props = ['bridge', 'mtu', 'domain', 'physical_function', 'addresses',
                          'interfaces', 'tag', 'vport', 'nat', 'ipv4_config', 'ipv6_config', 'dns'] -%}
network_{{ state }}:
  virt.network_{{ state }}:
    - name: {{ pillar['network_name'] }}
    - forward: {{ pillar['forward']|default('null') }}
    - bridge: {{ pillar['bridge']|default('null') }}
    - autostart: {{ pillar['autostart'] }}
{%- for property in optional_props -%}
    {{ optional(property) }}
{%- endfor %}
