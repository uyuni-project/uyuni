# SUSE Manager for Retail build trigger
#
{%- set root_dir   = '/var/lib/Kiwi/' + pillar.get('build_id') %}
{%- set dest_dir   = root_dir + '/images.build' %}
{%- set bundle_dir = root_dir + '/images/' %}
{%- set bundle_id  = pillar.get('build_id') %}

# the goal is to collect all information required for
# saltboot image pillar

mgr_inspect_kiwi_image:
  mgrcompat.module_run:
    - name: kiwi_info.inspect_image
    - dest: {{ dest_dir }}
    - bundle_dest: {{ bundle_dir }}

mgr_kiwi_cleanup:
  cmd.run:
    - name: "rm -rf '{{ root_dir }}'"
    - require:
      - mgrcompat: mgr_inspect_kiwi_image
