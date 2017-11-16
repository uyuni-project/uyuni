mgr_virt_destroy:
  module.run:
    - name: virt.purge
    - vm_: {{ pillar['domain_name'] }}
