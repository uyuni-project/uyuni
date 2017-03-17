mgr_image_profileupdate:
  module.run:
    - name: dockerng.sls_build
    - m_name: "{{ salt['pillar.get']('mgr_img_dummyname', 'dummy' ~ range(1, 1000) | random ) }}"
    - base: "{{ pillar.get('imagename') }}"
    - mods: packages.profileupdate
    - dryrun: True

mgr_image_inspect:
  module.run:
    - name: dockerng.inspect
    - m_name: "{{ pillar.get('imagename') }}"

mgr_container_remove:
  module.run:
    - name: dockerng.rmi
    - m_names:
      - "{{ pillar.get('imagename') }}"
    - force: False
    - require:
      - module: mgr_image_profileupdate
      - module: mgr_image_inspect
