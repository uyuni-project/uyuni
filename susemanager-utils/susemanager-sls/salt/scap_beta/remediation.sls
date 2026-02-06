# Applies custom Salt state content for SCAP remediation

{%- set remediation_content = pillar.get('scap_remediation_state') -%}

{%- if remediation_content -%}
{%- set remediation_states = remediation_content | load_yaml -%}
{%- for state_id, state_data in remediation_states.items() %}
{{ state_id }}:
{{ state_data | yaml(False) | indent(2, True) }}
{%- endfor %}

{%- else -%}

scap_remediation_skip:
  test.succeed_without_changes:
    - name: "Skipping: No SCAP remediation state provided in pillar"

{%- endif -%}
