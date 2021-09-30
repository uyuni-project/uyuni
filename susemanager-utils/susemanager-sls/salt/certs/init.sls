{% macro includesls(os_family) -%}
{% include 'certs/{0}.sls'.format(os_family) ignore missing -%}
{%- endmacro %}
{% set sls = includesls(grains['os_family']) -%}
