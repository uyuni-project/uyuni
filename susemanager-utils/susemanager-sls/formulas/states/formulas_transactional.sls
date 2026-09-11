{%- set unsupported_formulas = pillar.get("transactional_unsupported_formulas", []) %}
{%- if unsupported_formulas %}
{{ raise("Formulas do not support transactional systems: {}.".format(", ".join(unsupported_formulas))) }}
{%- endif %}
{%- set transactional_formulas = pillar.get("transactional_formulas", []) %}
{%- if transactional_formulas %}
include: {{ transactional_formulas }}
{%- endif %}
