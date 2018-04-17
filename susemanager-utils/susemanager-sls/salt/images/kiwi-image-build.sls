# SUSE Manager for Retail build trigger
#

{%- set source = pillar.get('source') %}

{%- set root_dir = '/var/lib/Kiwi/chroot/' + pillar.get('build_id') %}
{%- set dest_dir = '/var/lib/Kiwi/images/' + pillar.get('build_id') + '.build'  %}
{%- set bundle_dir = '/var/lib/Kiwi/images/' + pillar.get('build_id')%}
{%- set bundle_id = pillar.get('build_id')%}

{%- if pillar.get('activation_key') %}
{%- set kiwi_params = salt['cmd.run']('suma-repos ' + pillar.get('activation_key')) %}
{%- else %}
{%- set kiwi_params = '--add-repo ' + pillar.get('kiwi_repositories')|join(' --add-repo ') %}
{%- endif %}

{%- if pillar.get('use_build') %}
mgr_buildimage_build:
  cmd.run:
   - name: "/usr/bin/build {{ kvm }} --dist {{ dist }} --kiwi-parameter \"{{ kiwi_params }}\" --root {{ root_dir }} {{ build_repos }} {{ source }}"

{%- else %}

mgr_buildimage_kiwi_prepare:
  cmd.run:
    - name: "kiwi --nocolor --force-new-root --prepare {{ source }} --root {{ root_dir }} {{ kiwi_params }}"

mgr_buildimage_kiwi_create:
  cmd.run:
    - name: "kiwi --nocolor --yes --create {{ root_dir }} --dest {{ dest_dir }} {{ kiwi_params }}"
    - require:
      - cmd: mgr_buildimage_kiwi_prepare

mgr_buildimage_kiwi_bundle:
  cmd.run:
    - name: "kiwi --nocolor --yes --bundle-build {{ dest_dir }} --bundle-id {{ bundle_id }} --destdir {{ bundle_dir }} {{ kiwi_params }}"
    - require:
      - cmd: mgr_buildimage_kiwi_create


# push to salt store
mgr_buildimage_kiwi_collect_image:
  module.run:
    - name: cp.push_dir
    - path: {{ bundle_dir }}
    - require:
      - cmd: mgr_buildimage_kiwi_bundle

{%- endif %}
