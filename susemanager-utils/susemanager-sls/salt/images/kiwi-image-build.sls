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
{%- set kiwi_help = salt['cmd.run']('kiwi --help') %}
{%- set have_bundle_build = kiwi_help.find('--bundle-build') > 0 %}

# i586 build on x86_64 host must be called with linux32
# let's consider the build i586 if there is no x86_64 repo specified
{%- set kiwi = 'linux32 kiwi' if (pillar.get('kiwi_repositories')|join(' ')).find('x86_64') == -1 and grains.get('osarch') == 'x86_64' else 'kiwi' %}

# in SLES11 Kiwi the --add-repotype is required
{%- macro kiwi_params() -%}
  --add-repo {{ common_repo }} --add-repotype rpm-dir --add-repoalias common_repo {{ ' ' }}
{%- for repo in pillar.get('kiwi_repositories') -%}
  --add-repo {{ repo }} --add-repotype rpm-md --add-repoalias key_repo{{ loop.index }} {{ ' ' }}
{%- endfor -%}
{%- endmacro %}

mgr_buildimage_prepare_source:
  file.directory:
    - name: {{ root_dir }}
    - clean: True
  module.run:
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

mgr_buildimage_kiwi_prepare:
  cmd.run:
    - name: "{{ kiwi }} --nocolor --force-new-root --prepare {{ source_dir }} --root {{ chroot_dir }} {{ kiwi_params() }}"
    - require:
      - module: mgr_buildimage_prepare_source
      - file: mgr_buildimage_prepare_activation_key_in_source

mgr_buildimage_kiwi_create:
  cmd.run:
    - name: "{{ kiwi }} --nocolor --yes --create {{ chroot_dir }} --dest {{ dest_dir }} {{ kiwi_params() }}"
    - require:
      - cmd: mgr_buildimage_kiwi_prepare

{%- if have_bundle_build %}
mgr_buildimage_kiwi_bundle:
  cmd.run:
    - name: "{{ kiwi }} --nocolor --yes --bundle-build {{ dest_dir }} --bundle-id {{ bundle_id }} --destdir {{ bundle_dir }}"
    - require:
      - cmd: mgr_buildimage_kiwi_create

{%- else %}

# SLE11 Kiwi does not have --bundle-build option, we have to create the bundle tarball ourselves:

mgr_buildimage_kiwi_bundle_dir:
  file.directory:
    - name: {{ bundle_dir }}
    - require:
      - cmd: mgr_buildimage_kiwi_create

mgr_buildimage_kiwi_bundle_tarball:
  cmd.run:
    - name: "cd '{{ dest_dir }}' && tar czf '{{ bundle_dir }}'`basename *.packages .packages`-{{ bundle_id }}.tgz --no-recursion `find . -maxdepth 1 -type f`"
    - require:
      - file: mgr_buildimage_kiwi_bundle_dir

mgr_buildimage_kiwi_bundle:
  cmd.run:
    - name: "cd '{{ bundle_dir }}' && sha256sum *.tgz > `echo *.tgz`.sha256"
    - require:
      - cmd: mgr_buildimage_kiwi_bundle_tarball

{%- endif %}

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
