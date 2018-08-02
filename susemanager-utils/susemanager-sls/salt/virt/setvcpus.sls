mgr_virt_vcpus:
  module.run:
    - name: virt.setvcpus
    - vm_: {{ pillar['domain_name'] }}
    - vcpus: {{ pillar['domain_vcpus'] }}
    - config: True
