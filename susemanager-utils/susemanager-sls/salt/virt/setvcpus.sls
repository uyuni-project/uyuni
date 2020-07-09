mgr_virt_vcpus:
  mgrcompat.module_run:
    - name: virt.setvcpus
    - vm_: {{ pillar['domain_name'] }}
    - vcpus: {{ pillar['domain_vcpus'] }}
    - config: True
