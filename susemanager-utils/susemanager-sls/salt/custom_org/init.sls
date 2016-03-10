{% if pillar['org_id'] is defined and salt['file.file_exists']('/srv/susemanager/salt/custom/org_{0}.sls'.format(pillar['org_id'])) -%}
include:
- custom.org_{{ pillar['org_id'] }}
{% endif %}
