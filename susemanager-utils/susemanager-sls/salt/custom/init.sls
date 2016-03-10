{% if salt['file.file_exists']('/srv/susemanager/salt/custom/custom_{0}.sls'.format(grains['machine_id'])) -%}
include:
  - .custom_{{ grains['machine_id'] }}
{% endif %}

