{% do repos_disabled.update({'count': 0}) %}
{% set repos = salt['pkg.list_repos']() %}
{% for alias, data in repos.items() %}
{% if grains['os_family'] == 'Debian' %}
{% for entry in data %}
{% if entry.get('enabled', True) %}
{% set url = entry.get('uri') %}
{%- set repo_exists = (0 < salt['http.query'](url + '/Release', status=True, verify_ssl=True).get('status', 0) < 300) %}
{% if not repo_exists %} 
disable_repo_{{ repos_disabled.count }}:
  mgrcompat.module_run:
    - name: pkg.mod_repo
    - repo: {{ "'" ~ entry.line ~ "'" }}
    - kwargs:
        disabled: True
{% do repos_disabled.update({'count': repos_disabled.count + 1}) %}
{% endif %}
{% endif %}
{% endfor %}
{% else %}
{% if data.get('enabled', '1') == '1' %}
{% set url = data.get('baseurl', 'file://').replace('$basearch', grains['osarch']) -%}
{%- set repo_exists = (0 < salt['http.query'](url + '/repodata/repomd.xml', status=True, verify_ssl=True).get('status', 0) < 300) %}
{% if not repo_exists %}
disable_repo_{{ alias }}:
  mgrcompat.module_run:
    - name: pkg.mod_repo
    - repo: {{ alias }}
    - kwargs:
        enabled: False
    - require:
{%- if grains.get('__suse_reserved_saltutil_states_support', False) %}
      - saltutil: sync_states
{%- else %}
      - mgrcompat: sync_states
{%- endif %}
{% do repos_disabled.update({'count': repos_disabled.count + 1}) %}
{% endif %}
{% endif %}
{% endif %}
{% endfor %}
