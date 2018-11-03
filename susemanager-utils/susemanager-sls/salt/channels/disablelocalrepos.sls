# disable all local repos
# everything except susemanager:*
{% set repos = salt['pkg.list_repos']() %}
{% for alias, data in repos.items() %}
  {% if not 'susemanager:' in alias %}
    {% if data is sequence %}
      {# looks like Debian system #}
      {% for d in data %}
        {% if d.get('disabled', false) %}
        {% set repoline = d.get('line') %}
disable_{{ alias }}:
  module.run:
    - name: pkg.del_repo
    - repo: "{{ repoline }}"
        {% endif %}
      {% endfor %}
    {% else %}
      {% if data.get('enabled', true) %}
disable_{{ alias }}:
  module.run:
    - name: pkg.mod_repo
    - repo: {{ alias }}
    - kwargs:
        enabled: False
      {% endif %}
    {% endif %}
  {% endif %}
{% endfor %}
