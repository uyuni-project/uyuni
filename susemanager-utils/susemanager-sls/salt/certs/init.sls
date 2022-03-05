mgr_absent_ca_package:
  pkg.removed:
    - name: rhn-org-trusted-ssl-cert

{% macro includesls(os_family) -%}
{% include 'certs/{0}.sls'.format(os_family) -%}
{%- endmacro %}
{% set sls = includesls(grains['os_family']|lower) -%}
{{ sls }}
