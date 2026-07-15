#!jinja|yaml

{%- macro install_hardware_profile_prerequisites(require_sync_states=False) %}
{%- if grains['cpuarch'] in ['i386', 'i486', 'i586', 'i686', 'x86_64', 'aarch64'] %}
mgr_install_dmidecode:
  pkg.installed:
{%- if grains['os_family'] == 'Suse' and grains['osrelease'] in ['11.3', '11.4'] %}
    - name: pmtools
{%- else %}
    - name: dmidecode
{%- endif %}
{%- if require_sync_states %}
    - require:
{%- if grains.get('__suse_reserved_saltutil_states_support', False) %}
      - saltutil: sync_states
{%- else %}
      - module: sync_states
{%- endif %}
{%- endif %}
{%- endif %}
{%- endmacro %}
