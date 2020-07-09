vm_stopped:
  virt.powered_off:
    - name: {{ pillar['domain_name'] }}

mgr_virt_destroy:
  mgrcompat.module_run:
    - name: virt.purge
    - vm_: {{ pillar['domain_name'] }}
    - require:
      - virt: vm_stopped
