{% macro includesls(os_family, osrelease) -%}
{% include 'certs/{0}.sls'.format(os_family + osrelease.replace('.', '_')) ignore missing -%}
{%- endmacro %}
{% if grains['os_family'] == 'Suse' or  grains['os_family'] == 'Debian' %}
{% set sls = includesls(grains['os_family'], '') -%}
{% elif grains['os_family'] == 'RedHat' %}
{% if grains['osfullname'] == 'Alibaba Cloud Linux (Aliyun Linux)' %}
{% set sls = includesls('Alibaba', '2') -%}
{% else -%}
{% set sls = includesls(grains['os'], grains['osrelease']) -%}
{% endif -%}
{% if sls|trim != "" -%}
{{ sls }}
{% else -%}
{{ includesls(grains['os'], grains['osrelease_info']|first|string) }}
{% endif -%}
