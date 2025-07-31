#!jinja|yaml
# SUSE Multi-Linux Manager kiwi method detection
#
# Copyright (c) 2025 SUSE LLC

{%- set force_kiwi_ng = pillar.get('use_kiwi_ng', salt['pillar.get']('custom_info:use_kiwi_ng', False)) %}
{%- set force_kiwi_podman = salt['pillar.get']('custom_info:use_kiwi_container', False) %}

{%- set osfullname = salt['grains.get']('osfullname') %}
{%- set osmajorrelease = salt['grains.get']('osmajorrelease')|int() %}

{# on SLES11 and SLES12 use legacy Kiwi, use Kiwi NG elsewhere #}
{%- if osfullname == 'SLES' and osmajorrelease < 15 %}
{%-   set kiwi_method = 'legacy' %}
{%- elif osfullname == 'SLES' and osmajorrelease == 15 %}
{%-   set kiwi_method = 'kiwi-ng' %}
{%- else %}
{%-   set kiwi_method = 'podman' %}
{%- endif %}

{# handle overrides #}
{%- if force_kiwi_ng %}
{%-   set kiwi_method = 'kiwi-ng' %}
{%- elif force_kiwi_podman %}
{%-   set kiwi_method = 'podman' %}
{%- endif %}
