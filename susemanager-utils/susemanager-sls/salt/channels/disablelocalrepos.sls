# Disable all local repos matching or not matching the 'match_str'
# Default arguments: everything except *susemanager:*
{% if not repos_disabled is defined %}
{% set repos_disabled = {'match_str': 'susemanager:', 'matching': false} %}
{% endif %}
{% do repos_disabled.update({'count': 0}) %}

{% if salt['config.get']('disable_local_repos', True) %}
{% set repos = salt['pkg.list_repos']() %}
{% for alias, data in repos.items() %}
{% if grains['os_family'] == 'Debian' %}
{% for entry in data %}
{% if (repos_disabled.match_str in entry['file'])|string == repos_disabled.matching|string and entry.get('enabled', True) %} 
disable_repo_{{ repos_disabled.count }}:
  mgrcompat.module_run:
    - name: pkg.mod_repo
    - repo: {{ "'" ~ entry.line ~ "'" }}
    - kwargs:
        disabled: True
{% do repos_disabled.update({'count': repos_disabled.count + 1}) %}
{% endif %}
{% endfor %}
{% else %}
{% if (repos_disabled.match_str in alias)|string == repos_disabled.matching|string and data.get('enabled', True) in [True, '1'] %}
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
{% endfor %}
{% endif %}
