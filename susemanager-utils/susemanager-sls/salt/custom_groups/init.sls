{% for gid in pillar.get('group_ids', []) %}
{% include 'custom/group_{0}.sls'.format(gid) ignore missing %}
{% endfor %}
