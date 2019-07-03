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

jmx_exporter_tomcat_service:
  service.running:
    - name: prometheus-jmx_exporter@tomcat
    - enable: True
    - require:
      - cmd: jmx_exporter

jmx_exporter_taskomatic_systemd_config:
  file.managed:
    - name: /etc/prometheus-jmx_exporter/taskomatic/environment
    - makedirs: True
    - user: root
    - group: root
    - mode: 644
    - contents: |
        PORT="5557"
        EXP_PARAMS=""

jmx_exporter_taskomatic_yaml_config:
  file.managed:
    - name: /etc/prometheus-jmx_exporter/taskomatic/prometheus-jmx_exporter.yml
    - makedirs: True
    - user: root
    - group: root
    - mode: 644
    - contents: |
        hostPort: localhost:3334
        username:
        password:
        whitelistObjectNames:
          - java.lang:type=Threading,*
          - java.lang:type=Memory,*
          - Catalina:type=ThreadPool,name=*
        rules:
        - pattern: ".*"

jmx_exporter_taskomatic_service:
  service.running:
    - name: prometheus-jmx_exporter@taskomatic
    - enable: True
    - require:
      - cmd: jmx_exporter
      - file: jmx_exporter_taskomatic_systemd_config
      - file: jmx_exporter_taskomatic_yaml_config

