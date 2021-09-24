node_exporter:
  cmd.run:
    - name: /usr/bin/rpm --query --info golang-github-prometheus-node_exporter

node_exporter_service:
  service.running:
    - name: prometheus-node_exporter
    - enable: True
    - require:
      - cmd: node_exporter

postgres_exporter:
  cmd.run:
    - name: /usr/bin/rpm --query --info golang-github-wrouesnel-postgres_exporter

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
    - name: /etc/sysconfig/prometheus-postgres_exporter
    - source: salt://srvmonitoring/prometheus-postgres_exporter
    - template: jinja
    - user: root
    - group: root
    - mode: 644
    - require:
      - cmd: postgres_exporter
      - file: postgres_exporter_configuration
  service.running:
    - name: prometheus-postgres_exporter
    - enable: True
    - require:
      - file: postgres_exporter_service
    - watch:
      - file: postgres_exporter_configuration

jmx_exporter:
  cmd.run:
    - name: /usr/bin/rpm --query --info prometheus-jmx_exporter prometheus-jmx_exporter-tomcat

{% set remove_javaagent_props = {'service': 'tomcat', 'file': '/etc/sysconfig/tomcat'} %}
{%- include 'srvmonitoring/removejavaagentprops.sls' %}

jmx_exporter_tomcat_yaml_config:
  file.managed:
    - name: /etc/prometheus-jmx_exporter/tomcat/uyuni.yml
    - makedirs: True
    - user: root
    - group: root
    - mode: 644
    - contents: |
        whitelistObjectNames:
          - java.lang:type=Threading,*
          - java.lang:type=Memory,*
          - Catalina:type=ThreadPool,name=*
        rules:
        - pattern: ".*"

jmx_tomcat_config:
  cmd.run:
    - name: sed -i 's/JAVA_OPTS="\(.*\)"/JAVA_OPTS="\1 -javaagent:\/usr\/share\/java\/jmx_prometheus_javaagent.jar=5556:\/etc\/prometheus-jmx_exporter\/tomcat\/uyuni.yml"/' /etc/sysconfig/tomcat
    - require:
      - cmd: remove_tomcat_javaagent

{% set remove_javaagent_props = {'service': 'taskomatic', 'file': '/etc/rhn/taskomatic.conf'} %}
{%- include 'srvmonitoring/removejavaagentprops.sls' %}

jmx_exporter_taskomatic_yaml_config:
  file.managed:
    - name: /etc/prometheus-jmx_exporter/taskomatic/uyuni.yml
    - makedirs: True
    - user: root
    - group: root
    - mode: 644
    - contents: |
        whitelistObjectNames:
          - java.lang:type=Threading,*
          - java.lang:type=Memory,*
          - Catalina:type=ThreadPool,name=*
        rules:
        - pattern: ".*"

jmx_taskomatic_config:
  cmd.run:
    - name: sed -i 's/JAVA_OPTS="\(.*\)"/JAVA_OPTS="\1 -javaagent:\/usr\/share\/java\/jmx_prometheus_javaagent.jar=5557:\/etc\/prometheus-jmx_exporter\/taskomatic\/prometheus-jmx_exporter.yml"/' /etc/rhn/taskomatic.conf
    - require:
      - cmd: remove_taskomatic_javaagent

mgr_enable_prometheus_self_monitoring:
  cmd.run:
    - name: grep -q '^prometheus_monitoring_enabled.*=.*' /etc/rhn/rhn.conf && sed -i 's/^prometheus_monitoring_enabled.*/prometheus_monitoring_enabled = 1/' /etc/rhn/rhn.conf || echo 'prometheus_monitoring_enabled = 1' >> /etc/rhn/rhn.conf

mgr_is_prometheus_self_monitoring_enabled:
  cmd.run:
    - name: grep -qF 'prometheus_monitoring_enabled = 1' /etc/rhn/rhn.conf
