mgr_virt_suspend:
  mgrcompat.module_run:
    - name: virt.pause
    - vm_: {{ pillar['domain_name'] }}
