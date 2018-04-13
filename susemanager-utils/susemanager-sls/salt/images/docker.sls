{% if grains['saltversioninfo'][0] >= 2018 %}

mgr_registries_login:
  module.run:
    - name: docker.login
    - registries: {{ pillar.get('docker-registries', {}).keys() }}

mgr_buildimage:
  module.run:
    - name: docker.build
{%- if pillar.get('imagerepopath') is defined %}
    - repository: "{{ pillar.get('imagerepopath') }}"
    - tag: "{{ pillar.get('imagetag', 'latest') }}"
{%- else %}
    - repository: "{{ pillar.get('imagename') }}"
    - tag: "{{ pillar.get('imagename').rsplit(':', 1)[1] }}"
{%- endif %}
    - path: "{{ pillar.get('builddir') }}"
    - buildargs:
        repo: "{{ pillar.get('repo') }}"
        cert: "{{ pillar.get('cert') }}"
    - require:
      - module: mgr_registries_login

mgr_pushimage:
  module.run:
    - name: docker.push
    - image: "{{ pillar.get('imagename') }}"
    - require:
      - module: mgr_buildimage
      - module: mgr_registries_login

{% else %}

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

{% endif %}
