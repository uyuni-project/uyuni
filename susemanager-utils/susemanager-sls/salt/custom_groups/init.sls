{% for gid in pillar.get('group_id', []) %}
{% include 'custom/group_{0}.sls'.format(gid) ignore missing %}
{% endfor %}
