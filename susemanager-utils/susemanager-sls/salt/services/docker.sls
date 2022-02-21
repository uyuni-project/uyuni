{% if pillar['addon_group_types'] is defined and 'container_build_host' in pillar['addon_group_types'] %}
{% set use_venv_salt = salt['pkg.version']('venv-salt-minion') %}
mgr_install_docker:
  pkg.installed:
    - pkgs:
      - git-core
      - docker: '>=1.9.0'
{%- if not use_venv_salt %}
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
{%- if use_venv_salt %}
      - venv-salt-minion
{%- else %}
      - salt: '>=2016.11.1'
      - salt-minion: '>=2016.11.1'
{%- endif %}
    - order: last
{% endif %}
