{% include 'certs/{0}.sls'.format(grains['osfullname'] + grains['osrelease'].replace('.', '_')) ignore missing %}
