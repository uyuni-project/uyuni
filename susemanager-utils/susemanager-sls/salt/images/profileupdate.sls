{% set container_name = salt['pillar.get']('mgr_container_name', 'mgr_container_' ~ range(1, 10000) | random )  %}

{% if grains['saltversioninfo'][0] >= 2018 %}

mgr_registries_login_inspect:
  mgrcompat.module_run:
    - name: docker.login
    - registries: {{ pillar.get('docker-registries', {}).keys() | list }}

mgr_image_profileupdate:
  mgrcompat.module_run:
    - name: docker.sls_build
    - repository: "{{ container_name }}"
    - base: "{{ pillar.get('imagename') }}"
    - mods: packages.profileupdate
    - dryrun: True
    - kwargs:
        entrypoint: ""
    - require:
      - mgrcompat: mgr_registries_login_inspect

mgr_image_inspect:
  mgrcompat.module_run:
    - name: docker.inspect_image
    - m_name: "{{ pillar.get('imagename') }}"
    - require:
      - mgrcompat: mgr_registries_login_inspect

mgr_container_remove:
  mgrcompat.module_run:
    - name: docker.rm
    - args: [ "{{ container_name }}" ]
    - force: False
    - onlyif:
      - /usr/bin/docker ps -a | /usr/bin/grep "{{ container_name }}" >/dev/null

mgr_image_remove:
  mgrcompat.module_run:
    - name: docker.rmi
    - m_names:
      - "{{ pillar.get('imagename') }}"
    - force: False

{% if 'docker.logout' in salt %}

mgr_registries_logout:
  mgrcompat.module_run:
    - name: docker.logout
    - registries: {{ pillar.get('docker-registries', {}).keys() | list }}
    - require:
      - mgrcompat: mgr_registries_login_inspect
      - mgrcompat: mgr_image_profileupdate

{% endif %}

{% else %}

mgr_registries_login_inspect:
  mgrcompat.module_run:
    - name: dockerng.login
    - registries: {{ pillar.get('docker-registries', {}).keys() }}

mgr_image_profileupdate:
  mgrcompat.module_run:
    - name: dockerng.sls_build
    - m_name: "{{ container_name }}"
    - base: "{{ pillar.get('imagename') }}"
    - mods: packages.profileupdate
    - dryrun: True
    - kwargs:
        entrypoint: ""
    - require:
      - mgrcompat: mgr_registries_login_inspect

mgr_image_inspect:
  mgrcompat.module_run:
    - name: dockerng.inspect
    - m_name: "{{ pillar.get('imagename') }}"
    - require:
      - mgrcompat: mgr_registries_login_inspect

mgr_container_remove:
  mgrcompat.module_run:
    - name: dockerng.rm
    - args: [ "{{ container_name }}" ]
    - force: False
    - onlyif:
      - /usr/bin/docker ps -a | /usr/bin/grep "{{ container_name }}" >/dev/null

mgr_image_remove:
  mgrcompat.module_run:
    - name: dockerng.rmi
    - m_names:
      - "{{ pillar.get('imagename') }}"
    - force: False

{% endif %}

include:
  - util.syncstates
