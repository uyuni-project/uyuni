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
    - path: /usr/lib/systemd/system/tomcat.service.d/jmx.conf
    - pattern: "jmx_prometheus_javaagent.jar=5556"

jmx_taskomatic_java_config:
  mgrcompat.module_run:
    - name: file.search
    - path: /usr/lib/systemd/system/taskomatic.service.d/jmx.conf
    - pattern: "jmx_prometheus_javaagent.jar=5557"

mgr_is_prometheus_self_monitoring_enabled:
  cmd.run:
    - name: grep -q -E 'prometheus_monitoring_enabled\s*=\s*(1|y|true|yes|on)\s*$' /etc/rhn/rhn.conf

include:
  - util.syncstates
