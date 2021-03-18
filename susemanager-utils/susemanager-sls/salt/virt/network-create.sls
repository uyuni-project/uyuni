{% macro optional(name) -%}
  {%- if pillar[name]|default(none) %}
    - {{ name }}: {{ pillar[name] }}
  {%- endif %}
{%- endmacro %}

{% set  optional_props = ['bridge', 'mtu', 'domain', 'physical_function', 'addresses',
                          'interfaces', 'tag', 'vport', 'nat', 'ipv4_config', 'ipv6_config', 'dns'] -%}
network_running:
  virt.network_running:
    - name: {{ pillar['network_name'] }}
    - forward: {{ pillar['forward']|default('null') }}
    - bridge: {{ pillar['bridge']|default('null') }}
    - autostart: {{ pillar['autostart'] }}
{%- for property in optional_props -%}
    {{ optional(property) }}
{%- endfor %}
