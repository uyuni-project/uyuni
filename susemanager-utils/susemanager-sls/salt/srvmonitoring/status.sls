jmx_taskomatic_exporter_service:
  mgrcompat.module_run:
    - name: service.status
    - m_name: "prometheus-jmx_exporter@taskomatic.service"

jmx_tomcat_exporter_service:
  mgrcompat.module_run:
    - name: service.status
    - m_name: "prometheus-jmx_exporter@tomcat.service"

node_exporter_service:
  mgrcompat.module_run:
    - name: service.status
    - m_name: "prometheus-node_exporter.service"

postgres_exporter_service:
  mgrcompat.module_run:
    - name: service.status
    - m_name: "prometheus-postgres_exporter.service"

jmx_tomcat_java_config:
  mgrcompat.module_run:
    - name: file.search
    - path: /etc/sysconfig/tomcat
    - pattern: "-Dcom\\.sun\\.management\\.jmxremote\\.host=\\S* -Dcom\\.sun\\.management\\.jmxremote\\.port=3333 -Dcom\\.sun\\.management\\.jmxremote\\.ssl=false -Dcom\\.sun\\.management\\.jmxremote\\.authenticate=false -Djava\\.rmi\\.server\\.hostname="

jmx_taskomatic_java_config:
  mgrcompat.module_run:
    - name: file.search
    - path: /etc/rhn/taskomatic.conf
    - pattern: "-Dcom\\.sun\\.management\\.jmxremote\\.host=\\S* -Dcom\\.sun\\.management\\.jmxremote\\.port=3334 -Dcom\\.sun\\.management\\.jmxremote\\.ssl=false -Dcom\\.sun\\.management\\.jmxremote\\.authenticate=false -Djava\\.rmi\\.server\\.hostname="

mgr_is_prometheus_self_monitoring_enabled:
  cmd.run:
    - name: grep -q 'prometheus_monitoring_enabled\s*=\s*1\s*$' /etc/rhn/rhn.conf

include:
  - util.syncstates
