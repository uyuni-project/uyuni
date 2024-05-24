node_exporter:
  cmd.run:
    - name: /usr/bin/rpm --query --info golang-github-prometheus-node_exporter

node_exporter_service:
  service.running:
    - name: prometheus-node_exporter
    - enable: True
    - require:
      - cmd: node_exporter

{% set global = namespace(has_pillar_data = True) %}
{% for key in ['db_name', 'db_host', 'db_port', 'db_user', 'db_pass'] if global.has_pillar_data %}
  {% set global.has_pillar_data = key in pillar and pillar[key] %}
{% endfor %}

{% if global.has_pillar_data %}
postgres_exporter:
  cmd.run:
    - name: /usr/bin/rpm --query --info prometheus-postgres_exporter || /usr/bin/rpm --query --info golang-github-wrouesnel-postgres_exporter

postgres_exporter_cleanup:
  file.absent:
    - name: /etc/sysconfig/prometheus-postgres_exporter

postgres_exporter_configuration:
  file.managed:
    - name: /etc/postgres_exporter/postgres_exporter_queries.yaml
    - makedirs: True
    - source:
      - salt://srvmonitoring/postgres_exporter_queries.yaml
    - user: root
    - group: root
    - mode: 644

postgres_exporter_service:
  file.managed:
    - names:
      - /etc/systemd/system/prometheus-postgres_exporter.service.d/60-server.conf:
        - source: salt://srvmonitoring/prometheus-postgres_exporter
        - user: root
        - mode: 644
      - /etc/postgres_exporter/pg_passwd:
        - source: salt://srvmonitoring/pg_passwd
        - user: prometheus
        - mode: 600
    - makedirs: True
    - template: jinja
    - group: root
    - require:
      - cmd: postgres_exporter
      - file: postgres_exporter_configuration
  mgrcompat.module_run:
    - name: service.systemctl_reload
  service.running:
    - name: prometheus-postgres_exporter
    - enable: True
    - require:
      - file: postgres_exporter_service
    - watch:
      - file: postgres_exporter_configuration
{% endif %}

jmx_exporter:
  cmd.run:
    - name: /usr/bin/rpm --query --info prometheus-jmx_exporter

{% set remove_jmx_props = {'service': 'tomcat', 'file': '/etc/sysconfig/tomcat'} %}
{% include 'srvmonitoring/removejmxprops.sls' %}

jmx_exporter_tomcat_yaml_config:
  file.managed:
    - name: /etc/prometheus-jmx_exporter/tomcat/java_agent.yml
    - makedirs: True
    - user: root
    - group: root
    - mode: 644
    - source:
      - salt://srvmonitoring/java_agent.yaml

jmx_tomcat_config:
  file.managed:
    - name: /usr/lib/systemd/system/tomcat.service.d/jmx.conf
    - makedirs: True
    - user: root
    - group: root
    - mode: 644
    - source:
      - salt://srvmonitoring/tomcat_jmx.conf
    - require:
      - cmd: jmx_exporter
  mgrcompat.module_run:
    - name: service.systemctl_reload

jmx_exporter_tomcat_service_cleanup:
  service.dead:
    - name: prometheus-jmx_exporter@tomcat
    - enable: False

jmx_exporter_taskomatic_systemd_config_cleanup:
  file.absent:
    - name: /etc/prometheus-jmx_exporter/taskomatic/environment

{% set remove_jmx_props = {'service': 'taskomatic', 'file': '/etc/rhn/taskomatic.conf'} %}
{%- include 'srvmonitoring/removejmxprops.sls' %}

jmx_exporter_taskomatic_yaml_config_cleanup:
  file.absent:
    - name: /etc/prometheus-jmx_exporter/taskomatic/prometheus-jmx_exporter.yml

jmx_exporter_taskomatic_yaml_config:
  file.managed:
    - name: /etc/prometheus-jmx_exporter/taskomatic/java_agent.yml
    - makedirs: True
    - user: root
    - group: root
    - mode: 644
    - source:
      - salt://srvmonitoring/java_agent.yaml

jmx_taskomatic_config:
  file.managed:
    - name: /usr/lib/systemd/system/taskomatic.service.d/jmx.conf
    - makedirs: True
    - user: root
    - group: root
    - mode: 644
    - source:
      - salt://srvmonitoring/taskomatic_jmx.conf
    - require:
      - cmd: jmx_exporter
  mgrcompat.module_run:
    - name: service.systemctl_reload

jmx_exporter_taskomatic_service_cleanup:
  service.dead:
    - name: prometheus-jmx_exporter@taskomatic
    - enable: False

mgr_enable_prometheus_self_monitoring:
  cmd.run:
    - name: grep -q '^prometheus_monitoring_enabled.*=.*' /etc/rhn/rhn.conf && sed -i 's/^prometheus_monitoring_enabled.*/prometheus_monitoring_enabled = 1/' /etc/rhn/rhn.conf || echo 'prometheus_monitoring_enabled = 1' >> /etc/rhn/rhn.conf

mgr_is_prometheus_self_monitoring_enabled:
  cmd.run:
    - name: grep -qF 'prometheus_monitoring_enabled = 1' /etc/rhn/rhn.conf
