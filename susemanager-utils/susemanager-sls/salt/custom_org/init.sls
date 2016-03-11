{% if pillar['org_id'] is defined %}
{% include 'custom/org_{0}.sls'.format(pillar['org_id']) ignore missing %}
{% endif %}
