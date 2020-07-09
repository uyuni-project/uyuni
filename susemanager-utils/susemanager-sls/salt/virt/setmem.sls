mgr_virt_mem:
  mgrcompat.module_run:
    - name: virt.setmem
    - vm_: {{ pillar['domain_name'] }}
    - memory: {{ pillar['domain_mem'] // 1024 }}
    - config: True
