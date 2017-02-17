mgr_image_profileupdate:
  module.run:
    - name: dockerng.sls_build
    - m_name: "{{ salt['pillar.get']('mgr_img_dummyname', 'dummy') }}"
    - base: "{{ pillar.get('imagename') }}"
    - mods: packages.profileupdate
    - dryrun: True

mgr_container_remove:
  module.run:
    - name: dockerng.rm
    - m_name: "{{ salt['pillar.get']('mgr_img_dummyname', 'dummy') }}"
    - force: False
