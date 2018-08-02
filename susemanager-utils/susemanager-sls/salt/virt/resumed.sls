mgr_virt_resume:
  module.run:
    - name: virt.resume
    - vm_: {{ pillar['domain_name'] }}
