mgr_virt_mem:
  module.run:
    - name: virt.setmem
    - vm_: {{ pillar['domain_name'] }}
    - memory: {{ pillar['domain_mem'] // 1024 }}
    - config: True
