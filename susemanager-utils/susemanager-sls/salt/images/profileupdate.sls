{% set container_name = salt['pillar.get']('mgr_container_name', 'mgr_container_' ~ range(1, 10000) | random )  %}

{% if grains['saltversioninfo'][0] >= 2018 %}

mgr_registries_login_inspect:
  module.run:
    - name: docker.login
    - registries: {{ pillar.get('docker-registries', {}).keys() }}

mgr_image_profileupdate:
  module.run:
    - name: docker.sls_build
    - m_name: "{{ container_name }}"
    - base: "{{ pillar.get('imagename') }}"
    - mods: packages.profileupdate
    - dryrun: True
    - kwargs:
        entrypoint: ""
    - require:
      - module: mgr_registries_login_inspect

mgr_image_inspect:
  module.run:
    - name: docker.inspect
    - m_name: "{{ pillar.get('imagename') }}"
    - require:
      - module: mgr_registries_login_inspect

mgr_container_remove:
  module.run:
    - name: docker.rm
    - args: [ "{{ container_name }}" ]
    - force: False
    - onlyif:
      - docker ps -a | grep "{{ container_name }}" >/dev/null

mgr_image_remove:
  module.run:
    - name: docker.rmi
    - m_names:
      - "{{ pillar.get('imagename') }}"
    - force: False

{% else %}

mgr_registries_login_inspect:
  module.run:
    - name: dockerng.login
    - registries: {{ pillar.get('docker-registries', {}).keys() }}

mgr_image_profileupdate:
  module.run:
    - name: dockerng.sls_build
    - m_name: "{{ container_name }}"
    - base: "{{ pillar.get('imagename') }}"
    - mods: packages.profileupdate
    - dryrun: True
    - kwargs:
        entrypoint: ""
    - require:
      - module: mgr_registries_login_inspect

mgr_image_inspect:
  module.run:
    - name: dockerng.inspect
    - m_name: "{{ pillar.get('imagename') }}"
    - require:
      - module: mgr_registries_login_inspect

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

{% endif %}
