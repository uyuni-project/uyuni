node_exporter_service:
  service.dead:
    - name: prometheus-node_exporter
    - enable: False

postgres_exporter_service:
  service.dead:
    - name: prometheus-postgres_exporter
    - enable: False

jmx_exporter_tomcat_service:
  service.dead:
    - name: prometheus-jmx_exporter@tomcat
    - enable: False

jmx_exporter_taskomatic_service:
  service.dead:
    - name: prometheus-jmx_exporter@taskomatic
    - enable: False

