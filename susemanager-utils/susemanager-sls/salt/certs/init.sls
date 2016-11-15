{% if grains['os_family'] == 'Suse' %}
{% include ['certs/{0}.sls'.format(grains['osfullname'] + grains['osrelease'].replace('.', '_')), 'certs/{0}.sls'.format(grains['osfullname'] + grains['osrelease_info']|first|string) ] ignore missing %}
{% elif grains['os_family'] == 'RedHat' %}
{% include ['certs/{0}.sls'.format(grains['os'] + grains['osrelease'].replace('.', '_')), 'certs/{0}.sls'.format(grains['os'] + grains['osrelease_info']|first|string) ] ignore missing %}
{% endif %}
