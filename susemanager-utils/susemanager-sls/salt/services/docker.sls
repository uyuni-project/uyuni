{% if pillar['addon_group_types'] is defined and 'docker_build_host' in pillar['addon_group_types'] %}
mgr_install_docker:
  pkg.installed:
    - pkgs:
      - docker
      - python-docker-py
      - salt: '>=2016.11.1'

mgr_docker_service:
  service.running:
    - name: docker
    - enable: True
    - require:
      - pkg: mgr_install_docker
{% endif %}
