# disable all local repos
# everything except susemanager:*
{% set repos = salt['pkg.list_repos']() %}
{% if grains['os_family'] != 'Debian' %}
{% for alias, data in repos.items() %}
{% if not 'susemanager:' in alias %}
{% if data.get('enabled', true) %}
disable_{{ alias }}:
  module.run:
    - name: pkg.mod_repo
    - repo: {{ alias }}
    - kwargs:
        enabled: False
{% endif %}
{% endif %}
{% endfor %}
{% endif %}
