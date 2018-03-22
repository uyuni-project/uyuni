mgr_registries_login:
  module.run:
    - name: dockerng.login
    - registries: {{ pillar.get('docker-registries', {}).keys() }}

mgr_buildimage:
  module.run:
    - name: dockerng.build
    - image: "{{ pillar.get('imagename') }}"
    - path: "{{ pillar.get('builddir') }}"
    - buildargs:
        repo: "{{ pillar.get('repo') }}"
        cert: "{{ pillar.get('cert') }}"
    - require:
      - module: mgr_registries_login

mgr_pushimage:
  module.run:
    - name: dockerng.push
    - image: "{{ pillar.get('imagename') }}"
    - require:
      - module: mgr_buildimage
      - module: mgr_registries_login
