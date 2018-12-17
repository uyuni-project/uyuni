{% if pillar['virt_entitled'] %}
/etc/salt/minion.d/libvirt-events.conf:
  file.managed:
    - contents: |
        engines:
          - libvirt_events

{% else %}

/etc/salt/minion.d/libvirt-events.conf:
  file.absent

{% endif %}

salt-minion:
  service.running:
    - enable: True
    - watch:
      - file: /etc/salt/minion.d/libvirt-events.conf

