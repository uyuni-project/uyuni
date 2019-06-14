powered_off:
  virt.powered_off:
    - name: {{ pillar['domain_name'] }}

restarted:
  virt.running:
    - name: {{ pillar['domain_name'] }}
    - require:
      - virt: powered_off
