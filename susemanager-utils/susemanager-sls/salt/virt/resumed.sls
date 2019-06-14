mgr_virt_resume:
  mgrcompat.module_run:
    - name: virt.resume
    - vm_: {{ pillar['domain_name'] }}
