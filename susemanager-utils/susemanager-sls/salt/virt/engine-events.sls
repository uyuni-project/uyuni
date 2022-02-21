{% if pillar['virt_entitled'] %}
{% set minion_config_dir = salt["config.get"]("config_dir") %}
{{ minion_config_dir }}/minion.d/libvirt-events.conf:
  file.managed:
    - contents: |
        engines:
          - libvirt_events

/var/cache/virt_state.cache:
  file.absent

{% else %}

{{ minion_config_dir }}/minion.d/libvirt-events.conf:
  file.absent

{% endif %}
