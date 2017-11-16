mgr_virt_suspend:
  module.run:
    - name: virt.pause
    - vm_: {{ pillar['domain_name'] }}
