{% set container_name = salt['pillar.get']('mgr_container_name', 'mgr_container_' ~ range(1, 10000) | random )  %}

mgr_registries_login_inspect:
  module.run:
    - name: dockerng.login
    - registries: {{ pillar.get('docker-registries').keys() }}

mgr_image_profileupdate:
  module.run:
    - name: dockerng.sls_build
    - m_name: "{{ container_name }}"
    - base: "{{ pillar.get('imagename') }}"
    - mods: packages.profileupdate
    - dryrun: True
    - kwargs:
        entrypoint: ""

mgr_image_inspect:
  module.run:
    - name: dockerng.inspect
    - m_name: "{{ pillar.get('imagename') }}"

mgr_container_remove:
  module.run:
    - name: dockerng.rm
    - args: [ "{{ container_name }}" ]
    - force: False
    - onlyif:
      - docker ps -a | grep "{{ container_name }}" >/dev/null

mgr_image_remove:
  module.run:
    - name: dockerng.rmi
    - m_names:
      - "{{ pillar.get('imagename') }}"
    - force: False
