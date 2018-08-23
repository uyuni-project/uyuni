# SUSE Manager for Retail build trigger
#

{%- set source     = pillar.get('source') %}

{%- set kiwi_dir   = '/var/lib/Kiwi/' %}
{%- set common_repo = kiwi_dir + 'repo' %}

{%- set root_dir   = kiwi_dir + pillar.get('build_id') %}
{%- set source_dir = root_dir + '/source' %}
{%- set chroot_dir = root_dir + '/chroot/' %}
{%- set dest_dir   = root_dir + '/images.build' %}
{%- set bundle_dir = root_dir + '/images/' %}
{%- set bundle_id  = pillar.get('build_id') %}
{%- set activation_key = pillar.get('activation_key') %}

{%- set kiwi_params = '--add-repo ' + common_repo + ' --add-repo ' + pillar.get('kiwi_repositories')|join(' --add-repo ') %}
mgr_buildimage_prepare_source:
  file.directory:
    - name: {{ root_dir }}
    - clean: True
  module.run:
    - name: kiwi_source.prepare_source
    - source: {{ source }}
    - root: {{ root_dir }}
    - activation_key: {{ activation_key }}

mgr_buildimage_kiwi_prepare:
  cmd.run:
    - name: "kiwi --nocolor --force-new-root --prepare {{ source_dir }} --root {{ chroot_dir }} {{ kiwi_params }}"
    - require:
      - module: mgr_buildimage_prepare_source

mgr_buildimage_kiwi_create:
  cmd.run:
    - name: "kiwi --nocolor --yes --create {{ chroot_dir }} --dest {{ dest_dir }} {{ kiwi_params }}"
    - require:
      - cmd: mgr_buildimage_kiwi_prepare

mgr_buildimage_kiwi_bundle:
  cmd.run:
    - name: "kiwi --nocolor --yes --bundle-build {{ dest_dir }} --bundle-id {{ bundle_id }} --destdir {{ bundle_dir }} {{ kiwi_params }}"
    - require:
      - cmd: mgr_buildimage_kiwi_create


{%- if pillar.get('use_salt_transport') %}
mgr_buildimage_kiwi_collect_image:
  module.run:
    - name: cp.push_dir
    - path: {{ bundle_dir }}
    - require:
      - cmd: mgr_buildimage_kiwi_bundle
{%- endif %}

mgr_buildimage_info:
  module.run:
    - name: kiwi_info.image_details
    - dest: {{ dest_dir }}
    - bundle_dest: {{ bundle_dir }}
    - require:
{%- if pillar.get('use_salt_transport') %}
      - mgr_buildimage_kiwi_collect_image
{%- else %}
      - mgr_buildimage_kiwi_bundle
{%- endif %}
