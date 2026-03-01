mgr_virt_profile:
  mgrcompat.module_run:
    - name: virt.vm_info
    - onlyif:
      - virsh list

