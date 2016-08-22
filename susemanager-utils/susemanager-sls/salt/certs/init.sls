{% if grains['os_family'] == 'Suse' %}
{% include 'certs/{0}.sls'.format(grains['osfullname'] + grains['osrelease'].replace('.', '_')) ignore missing %}
{% elif grains['os_family'] == 'RedHat' %}
{% include 'certs/{0}.sls'.format(grains['os'] + grains['osrelease'].replace('.', '_')) ignore missing %}
{% endif %}
