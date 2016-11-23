{% macro includesls(osfullname, osrelease) -%}
{% include 'certs/{0}.sls'.format(osfullname + osrelease.replace('.', '_')) ignore missing -%}
{%- endmacro %}
{% if grains['os_family'] == 'Suse' %}
{% set sls = includesls(grains['osfullname'], grains['osrelease']) -%}
{% if sls|trim != "" -%}
{{ sls }}
{% else -%}
{{ includesls(grains['osfullname'], grains['osrelease_info']|first|string) }}
{% endif -%}
{% elif grains['os_family'] == 'RedHat' %}
{% set sls = includesls(grains['os'], grains['osrelease']) -%}
{% if sls|trim != "" -%}
{{ sls }}
{% else -%}
{{ includesls(grains['os'], grains['osrelease_info']|first|string) }}
{% endif -%}
{% endif %}