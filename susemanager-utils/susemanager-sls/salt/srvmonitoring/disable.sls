node_exporter_service:
  service.dead:
    - name: prometheus-node_exporter
    - enable: False

postgres_exporter_service:
  service.dead:
    - name: prometheus-postgres_exporter
    - enable: False

jmx_tomcat_config:
  file.absent:
    - name: /usr/lib/systemd/system/tomcat.service.d/jmx.conf
  mgrcompat.module_run:
    - name: service.systemctl_reload

jmx_taskomatic_config:
  file.absent:
    - name: /usr/lib/systemd/system/taskomatic.service.d/jmx.conf
  mgrcompat.module_run:
    - name: service.systemctl_reload

mgr_enable_prometheus_self_monitoring:
  cmd.run:
    - name: grep -q '^prometheus_monitoring_enabled.*=.*' /etc/rhn/rhn.conf && sed -i 's/^prometheus_monitoring_enabled.*/prometheus_monitoring_enabled = 0/' /etc/rhn/rhn.conf || echo 'prometheus_monitoring_enabled = 0' >> /etc/rhn/rhn.conf

mgr_is_prometheus_self_monitoring_disabled:
  cmd.run:
    - name: grep -qF 'prometheus_monitoring_enabled = 0' /etc/rhn/rhn.conf
