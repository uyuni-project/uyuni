mgr_virt_profile:
  module.run:
    - name: virt.vm_info
    - onlyif:
      - virsh list

