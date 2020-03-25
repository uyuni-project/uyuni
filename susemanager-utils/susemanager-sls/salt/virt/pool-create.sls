{% set pool_state = salt.virt.pool_info(pillar['pool_name']).get(pillar['pool_name'], {}).get('state') %}
{% set state = 'running' if pool_state == 'running' else pillar['action_type'] %}

pool_{{ state }}:
  virt.pool_{{ state }}:
    - name: {{ pillar['pool_name'] }}
    - ptype: {{ pillar['pool_type'] }}
    {% if pillar['target']|default(none) %}
    - target: {{ pillar['target'] }}
    {% endif %}
    - autostart: {{ pillar['autostart'] }}
    {% if pillar['permissions']|default(none) %}
    - permissions:
      {% if pillar['permissions']['mode']|default(none) %}
        mode: {{ pillar['permissions']['mode'] }}
      {% endif %}
      {% if pillar['permissions']['owner']|default(none) %}
        owner: {{ pillar['permissions']['owner'] }}
      {% endif %}
      {% if pillar['permissions']['group']|default(none) %}
        group: {{ pillar['permissions']['group'] }}
      {% endif %}
      {% if pillar['permissions']['label']|default(none) %}
        label: {{ pillar['permissions']['label'] }}
      {% endif %}
    {% endif %}  {# pillar['permissions']['mode']|default(none) #}
    {% if pillar['source']|default(none) %}
    - source:
      {% if pillar['source']['dir']|default(none) %}
        dir: {{ pillar['source']['dir'] }}
      {% endif %}
      {% if pillar['source']['name']|default(none) %}
        name: {{ pillar['source']['name'] }}
      {% endif %}
      {% if pillar['source']['format']|default(none) %}
        format: {{ pillar['source']['format'] }}
      {% endif %}
      {% if pillar['source']['initiator']|default(none) %}
        initiator: {{ pillar['source']['initiator'] }}
      {% endif %}
      {% if pillar['source']['hosts']|default(none) %}
        hosts:
        {% for host in pillar['source']['hosts'] %}
          - {{ host }}
        {% endfor %}
      {% endif %}  {# pillar['source']['hosts']|default(none) #}
      {% if pillar['source']['auth']|default(none) %}
        auth:
          username: {{ pillar['source']['auth']['username'] }}
          password: {{ pillar['source']['auth']['password'] }}
      {% endif %}  {# pillar['source']['auth']|default(none) #}
      {% if pillar['source']['devices']|default(none) %}
        devices:
        {% for device in pillar['source']['devices'] %}
          - path: {{ device['path'] }}
          {% if device['part_separator']|default(none) %}
            part_separator: {{ device['part_separator'] }}
          {% endif %}
        {% endfor %}
      {% endif %}  {# pillar['source']['devices']|default(none) #}
      {% if pillar['source']['adapter']|default(none) %}
        adapter:
        {% if pillar['source']['adapter']['type']|default(none) %}
          type: {{ pillar['source']['adapter']['type'] }}
        {% endif %}
        {% if pillar['source']['adapter']['name']|default(none) %}
          name: {{ pillar['source']['adapter']['name'] }}
        {% endif %}
        {% if pillar['source']['adapter']['parent']|default(none) %}
          parent: {{ pillar['source']['adapter']['parent'] }}
        {% endif %}
        {% if pillar['source']['adapter']['managed']|default(none) %}
          managed: {{ pillar['source']['adapter']['managed'] }}
        {% endif %}
        {% if pillar['source']['adapter']['parent_wwnn']|default(none) %}
          parent_wwnn: {{ pillar['source']['adapter']['parent_wwnn'] }}
        {% endif %}
        {% if pillar['source']['adapter']['parent_wwpn']|default(none) %}
          parent_wwpn: {{ pillar['source']['adapter']['parent_wwpn'] }}
        {% endif %}
        {% if pillar['source']['adapter']['parent_fabric_wwn']|default(none) %}
          parent_fabric_wwn: {{ pillar['source']['adapter']['parent_fabric_wwn'] }}
        {% endif %}
        {% if pillar['source']['adapter']['wwnn']|default(none) %}
          wwnn: {{ pillar['source']['adapter']['wwnn'] }}
        {% endif %}
        {% if pillar['source']['adapter']['wwpn']|default(none) %}
          wwpn: {{ pillar['source']['adapter']['wwpn'] }}
        {% endif %}
        {% if pillar['source']['adapter']['parent_address']|default(none) %}
          parent_address:
          {% if pillar['source']['adapter']['parent_address']['unique_id']|default(none) %}
            unique_id: {{ pillar['source']['adapter']['parent_address']['unique_id'] }}
          {% endif %}
          {% if pillar['source']['adapter']['parent_address']['address']|default(none) %}
            address:
            {% if pillar['source']['adapter']['parent_address']['address']['domain']|default(none) %}
              domain: {{ pillar['source']['adapter']['parent_address']['address']['domain'] }}
            {% endif %}
            {% if pillar['source']['adapter']['parent_address']['address']['bus']|default(none) %}
              bus: {{ pillar['source']['adapter']['parent_address']['address']['bus'] }}
            {% endif %}
            {% if pillar['source']['adapter']['parent_address']['address']['slot']|default(none) %}
              slot: {{ pillar['source']['adapter']['parent_address']['address']['slot'] }}
            {% endif %}
            {% if pillar['source']['adapter']['parent_address']['address']['function']|default(none) %}
              function: {{ pillar['source']['adapter']['parent_address']['address']['function'] }}
            {% endif %}
          {% endif %}  {# pillar['source']['adapter']['parent_address']['address']|default(none) #}
        {% endif %}  {# pillar['source']['adapter']['parent_address']|default(none) #}
      {% endif %}  {# pillar['source']['adapter']|default(none) #}
    {% endif %}  {# pillar['source']|default(none) #}
