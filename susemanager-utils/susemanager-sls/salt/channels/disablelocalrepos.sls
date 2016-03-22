# disable all local repos
# everything except susemanager:*
{% set repos = salt['pkg.list_repos']() %}
{% for alias, data in repos.iteritems() %}
{% if not 'susemanager:' in alias %}
{% if data['enabled'] %}
disable_{{ alias }}:
  module.run:
    - name: pkg.mod_repo
    - repo: {{ alias }}
    - kwargs:
        enabled: False
{% endif %}
{% endif %}
{% endfor %}
