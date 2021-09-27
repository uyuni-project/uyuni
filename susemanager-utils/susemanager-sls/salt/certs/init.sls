{% if grains['os_family'] == 'Suse' %}
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
{% elif grains['os_family'] == 'Debian' %}
{% if grains ['os'] == 'AstraLinuxCE' %}
{{ includesls(grains['os'], grains['oscodename']) }}
{% else %}
{{ includesls(grains['os'], grains['osrelease_info']|first|string) }}
{% endif %}
{% endif %}
