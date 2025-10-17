#!jinja|yaml
# SUSE Multi-Linux Manager image build state
#
# Copyright (c) 2017 - 2025 SUSE LLC

{% from "images/kiwi-detect.sls" import kiwi_method with context %}

{%- set source     = pillar.get('source') %}

{%- set kiwi_dir   = '/var/lib/Kiwi/' %}
{%- set common_repo = kiwi_dir + 'repo' %}

{%- set build_id   = pillar.get('build_id') %}
{%- set root_dir   = kiwi_dir + build_id %}
{%- set source_dir = root_dir + '/source' %}
{%- set chroot_dir = root_dir + '/chroot/' %}
{%- set dest_dir   = root_dir + '/images.build' %}
{%- set bundle_dir = root_dir + '/images/' %}
{%- set cache_dir  = root_dir + '/cache/' %}

{%- set eib_require = '' %}
{%- set kpartx_require = '' %}

{%- set activation_key = pillar.get('activation_key') %}
{%- set use_bundle_build = pillar.get('use_bundle_build', salt['pillar.get']('custom_info:use_bundle_build', False)) %}

{# Default images and overrides #}
{%- set eib_image = salt['pillar.get']('custom_info:eib_image', 'registry.suse.com/edge/3.2/edge-image-builder:1.1.0') %}
{%- set kiwi_image = salt['pillar.get']('custom_info:kiwi_image', 'registry.suse.com/bci/kiwi:10.2') %}

mgr_buildimage_prepare_source:
  file.directory:
    - name: {{ root_dir }}
    - clean: True
  mgrcompat.module_run:
    - name: kiwi_source.prepare_source
    - source: {{ source }}
    - root: {{ root_dir }}

mgr_buildimage_prepare_activation_key_in_source:
  file.managed:
    - name: {{ source_dir }}/root/etc/salt/minion.d/kiwi_activation_key.conf
    - makedirs: True
    - contents: |
        grains:
          susemanager:
            activation_key: {{ activation_key }}

{%- if kiwi_method == 'podman' %}
{%- set kpartx_require = '- file: mgr_buildimage_prepare_kpartx_kiwi_yml' %}
{%- set eib_require = '- cmd: mgr_eib' %}
mgr_buildimage_prepare_kpartx_kiwi_yml:
  file.managed:
    - name: {{ source_dir }}/kiwi.yml
    - contents: |
        mapper:
          - part_mapper: kpartx

# EIB support
mgr_eib:
  file.directory:
    - name: {{ source_dir }}/root/oem
    - onlyif:
      - test -f {{ source_dir }}/eib/eib.yaml
  cmd.run:
    - names:
      - podman run --rm --privileged -v {{ source_dir }}/eib:/eib:ro,Z {{ eib_image }} build --definition-file=eib.yaml
      - xorriso -osirrox on -indev {{ source_dir }}/eib/combustion.iso extract / {{ source_dir }}/root/oem
    - require:
      - file: mgr_eib
    - onlyif:
      - test -f {{ source_dir }}/eib/eib.yaml

{# need ca-certificates for kiwi to trust CA #}
{# need /dev for losetup error during create #}
{% set kiwi_mount = ' -v '+ kiwi_dir + ':/var/lib/Kiwi:Z ' %}
{% set kiwi_yml_mount = ' -v ' + source_dir + '/kiwi.yml:/etc/kiwi.yml:ro,Z ' %}
{%- set kiwi = '/usr/bin/podman run --rm --privileged -v /var/lib/ca-certificates:/var/lib/ca-certificates:ro -v /dev:/dev '+ kiwi_mount + kiwi_yml_mount + kiwi_image + ' kiwi-ng' -%}
{%- elif kiwi_method == 'kiwi-ng' -%}
{%- set kiwi = '/usr/bin/kiwi-ng' -%}
{%- endif -%} {# kiwi_method #}

{%- if kiwi_method == 'podman' or kiwi_method == 'kiwi-ng' %}
{%- set kiwi_options = pillar.get('kiwi_options', '') %}
{%- set bootstrap_packages = ['findutils', 'rhn-org-trusted-ssl-cert-osimage'] %}

{%- macro kiwi_params() -%}
  --ignore-repos-used-for-build --add-repo file:{{ common_repo }},rpm-dir,common_repo,90,false,false
{% for pkg in bootstrap_packages -%}
  --add-bootstrap-package {{ pkg }}
{% endfor -%}
{%- for repo in pillar.get('kiwi_repositories') -%}
  --add-repo {{ repo }},rpm-md,key_repo{{ loop.index }},90,false,false {{ ' ' }}
{%- endfor -%}
{%- endmacro %}

{# we need to remove rpm-md due to kiwi error during create #}
mgr_buildimage_kiwi_prepare:
  cmd.run:
    - name: "{{ kiwi }} {{ kiwi_options }} $GLOBAL_PARAMS system prepare $PARAMS"
    - hide_output: True
    - env:
      - GLOBAL_PARAMS: "--logfile={{ root_dir }}/build.log --shared-cache-dir={{ cache_dir }}"
      - PARAMS:  "--description {{ source_dir }} --root {{ chroot_dir }} {{ kiwi_params() }}"
    - require:
      - mgrcompat: mgr_buildimage_prepare_source
      - file: mgr_buildimage_prepare_activation_key_in_source
      {{ kpartx_require }}
      {{ eib_require }}

mgr_buildimage_kiwi_create:
  cmd.run:
    - name: "{{ kiwi }} --logfile={{ root_dir }}/build.log --shared-cache-dir={{ cache_dir }} {{ kiwi_options }} system create --root {{ chroot_dir }} --target-dir  {{ dest_dir }}"
    - require:
      - cmd: mgr_buildimage_kiwi_prepare

{%- if use_bundle_build %}
mgr_buildimage_kiwi_bundle:
  cmd.run:
    - name: "{{ kiwi }} result bundle --target-dir {{ dest_dir }} --id {{ build_id }} --bundle-dir {{ bundle_dir }}"
    - require:
      - cmd: mgr_buildimage_kiwi_create
{%- endif %}

{%- else %} {# kiwi legacy #}

# i586 build on x86_64 host must be called with linux32
# let's consider the build i586 if there is no x86_64 repo specified
{%- set kiwi = '/usr/bin/linux32 /usr/sbin/kiwi' if (pillar.get('kiwi_repositories')|join(' ')).find('x86_64') == -1 and grains.get('osarch') == 'x86_64' else '/usr/sbin/kiwi' %}

# in SLES11 Kiwi the --add-repotype is required
{%- macro kiwi_params() -%}
  --add-repo {{ common_repo }} --add-repotype rpm-dir --add-repoalias common_repo {{ ' ' }}
{%- for repo in pillar.get('kiwi_repositories') -%}
  --add-repo {{ repo }} --add-repotype rpm-md --add-repoalias key_repo{{ loop.index }} {{ ' ' }}
{%- endfor -%}
{%- endmacro %}

# old Kiwi can't change cache location, so we have to clear cache before each build
mgr_kiwi_clear_cache:
  file.directory:
    - name: /var/cache/kiwi/
    - makedirs: True
    - clean: True

mgr_buildimage_kiwi_prepare:
  cmd.run:
    - name: "{{ kiwi }} --logfile {{ root_dir }}/build.log --nocolor --force-new-root --prepare {{ source_dir }} --root {{ chroot_dir }} {{ kiwi_params() }}"
    - require:
      - mgrcompat: mgr_buildimage_prepare_source
      - file: mgr_buildimage_prepare_activation_key_in_source

mgr_buildimage_kiwi_create:
  cmd.run:
    - name: "{{ kiwi }} --logfile {{ root_dir }}/build.log --nocolor --yes --create {{ chroot_dir }} --dest {{ dest_dir }} {{ kiwi_params() }}"
    - require:
      - cmd: mgr_buildimage_kiwi_prepare

{%- if use_bundle_build %}
mgr_buildimage_kiwi_bundle:
  cmd.run:
    - name: "{{ kiwi }} --nocolor --yes --bundle-build {{ dest_dir }} --bundle-id {{ build_id }} --destdir {{ bundle_dir }}"
    - require:
      - cmd: mgr_buildimage_kiwi_create

{%- endif %} {# use_bundle_build #}
{%- endif %} {# else kiwi legacy #}

{%- if pillar.get('use_salt_transport') %}
mgr_buildimage_kiwi_collect_image:
  mgrcompat.module_run:
    - name: cp.push_dir
    {%- if use_bundle_build %}
    - path: {{ bundle_dir }}
    - require:
      - cmd: mgr_buildimage_kiwi_bundle
    {%- else %}
    - path: {{ dest_dir }}
    - require:
      - cmd: mgr_buildimage_kiwi_create
    {%- endif %}
{%- endif %} {# use_salt_transport #}

mgr_buildimage_info:
  mgrcompat.module_run:
    - name: kiwi_info.build_info
    - dest: {{ dest_dir }}
    - build_id: {{ build_id }}
    {%- if use_bundle_build %}
    - bundle_dest: {{ bundle_dir }}
    {%- endif %}
    - require:
{%- if pillar.get('use_salt_transport') %}
      - mgr_buildimage_kiwi_collect_image
{%- else %}
    {%- if use_bundle_build %}
      - mgr_buildimage_kiwi_bundle
    {%- else %}
      - mgr_buildimage_kiwi_create
    {%- endif %} {# use_bundle_build #}
{%- endif %} {# use_salt_transport #}

mgr_buildimage_kiwi_collect_logs:
  mgrcompat.module_run:
    - name: cp.push
    - path: {{ root_dir }}/build.log
    - upload_path: /image-{{ build_id }}.log
    - order: last
