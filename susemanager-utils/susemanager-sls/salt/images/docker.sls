{% set logfile = "/var/log/image-" + pillar.get('build_id') + ".log" %}
{% if grains['saltversioninfo'][0] >= 2018 %}

mgr_registries_login:
  mgrcompat.module_run:
    - name: docker.login
    - registries: {{ pillar.get('docker-registries', {}).keys() | list }}

mgr_buildimage:
  mgrcompat.module_run:
    - name: docker.build
{%- if pillar.get('imagerepopath') is defined %}
    - repository: "{{ pillar.get('imagerepopath') }}"
    - tag: "{{ pillar.get('imagetag', 'latest') }}"
{%- else %}
    - repository: "{{ pillar.get('imagename') }}"
    - tag: "{{ pillar.get('imagename').rsplit(':', 1)[1] }}"
{%- endif %}
    - path: "{{ pillar.get('builddir') }}"
{%- if grains['saltversioninfo'][0] >= 3002 %}
    - logfile: {{ logfile }}
{%- endif %}
    - buildargs:
        repo: "{{ pillar.get('repo') }}"
        cert: "{{ pillar.get('cert') }}"
{%- if pillar.get('customvalues') is defined %}
{%- for key, value in pillar.get('customvalues').items() %}
        {{key}}: "{{value}}"
{%- endfor %}
{%- endif %}
    - require:
      - mgrcompat: mgr_registries_login

mgr_pushimage:
  mgrcompat.module_run:
    - name: docker.push
    - image: "{{ pillar.get('imagename') }}"
    - require:
      - mgrcompat: mgr_buildimage
      - mgrcompat: mgr_registries_login

{% if 'docker.logout' in salt %}

mgr_registries_logout:
  mgrcompat.module_run:
    - name: docker.logout
    - registries: {{ pillar.get('docker-registries', {}).keys() | list }}
    - require:
      - mgrcompat: mgr_pushimage
      - mgrcompat: mgr_registries_login

{% endif %}

{% else %}

mgr_registries_login:
  mgrcompat.module_run:
    - name: dockerng.login
    - registries: {{ pillar.get('docker-registries', {}).keys() }}

mgr_buildimage:
  mgrcompat.module_run:
    - name: dockerng.build
    - image: "{{ pillar.get('imagename') }}"
    - path: "{{ pillar.get('builddir') }}"
    - buildargs:
        repo: "{{ pillar.get('repo') }}"
        cert: "{{ pillar.get('cert') }}"
{%- if pillar.get('customvalues') is defined %}
{%- for key, value in pillar.get('customvalues').items() %}
        {{key}}: "{{value}}"
{%- endfor %}
{%- endif %}
    - require:
      - mgrcompat: mgr_registries_login

mgr_pushimage:
  mgrcompat.module_run:
    - name: dockerng.push
    - image: "{{ pillar.get('imagename') }}"
    - require:
      - mgrcompat: mgr_buildimage
      - mgrcompat: mgr_registries_login

{% endif %}

{%- if grains['saltversioninfo'][0] >= 3002 %}
mgr_buildimage_docker_collect_logs:
  file.touch:
    - name: {{ logfile }}
  mgrcompat.module_run:
    - name: cp.push
    - path: {{ logfile }}
    - upload_path: /image-{{ pillar.get('build_id') }}.log
    - order: last
{%- endif %}
