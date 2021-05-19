{% if pillar['addon_group_types'] is defined and 'container_build_host' in pillar['addon_group_types'] %}
mgr_install_docker:
  pkg.installed:
    - pkgs:
      - git-core
      - docker: '>=1.9.0'
{%- if grains['pythonversion'][0] == 3 %}
    {%- if grains['osmajorrelease'] == 12 %}
      - python3-docker-py: '>=1.6.0'
    {%- else %}
      - python3-docker: '>=1.6.0'
    {%- endif %}
{%- else %}
      - python-docker-py: '>=1.6.0'
{%- endif %}
{%- if grains['saltversioninfo'][0] >= 2018 %}
      - python3-salt
    {%- if grains['saltversioninfo'][0] < 3002 and salt['pkg.info_available']('python-Jinja2', 'python2-Jinja2') and salt['pkg.info_available']('python', 'python2') and salt['pkg.info_available']('python2-salt') %}
      - python2-salt
    {%- endif %}
{%- endif %}

mgr_docker_service:
  service.running:
    - name: docker
    - enable: True
    - require:
      - pkg: mgr_install_docker

mgr_min_salt:
  pkg.installed:
    - pkgs:
      - salt: '>=2016.11.1'
      - salt-minion: '>=2016.11.1'
    - order: last
{% endif %}
