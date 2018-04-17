# SUSE Manager for Retail build trigger
#

{%- set root_dir = '/var/lib/Kiwi/chroot/' + pillar.get('build_id') %}
{%- set dest_dir = '/var/lib/Kiwi/images/' + pillar.get('build_id') + '.build'  %}
{%- set bundle_dir = '/var/lib/Kiwi/images/' + pillar.get('build_id')%}
{%- set bundle_id = pillar.get('build_id')%}


{%- if pillar.get('use_build') %}
#mgr_buildimage_build:

{%- else %}

# the goal is to collect all information required for
# saltboot image pillar

mgr_inspect_kiwi_image:
  module.run:
    - name: kiwi_info.inspect_image
    - dest: {{ dest_dir }}
    - bundle_dest: {{ bundle_dir }}

mgr_kiwi_cleanup:
  cmd.run:
    - name: "rm -rf '{{ root_dir }}' '{{ bundle_dir }}' '{{ dest_dir }}'"
    - require:
      - module: mgr_inspect_kiwi_image

{%- endif %}
