buildimage:
  module.run:
    - name: dockerng.build
    - image: "{{ pillar.get('imagename') }}"
    - path: "{{ pillar.get('builddir') }}"
    - buildargs:
        repo: "{{ pillar.get('repo') }}"
        cert: "{{ pillar.get('cert') }}"

pushimage:
  module.run:
    - name: dockerng.push
    - image: "{{ pillar.get('imagename') }}"
    - require:
      - module: buildimage
