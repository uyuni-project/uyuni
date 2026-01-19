node_exporter_service:
  service.dead:
    - name: prometheus-node_exporter
    - enable: False

postgres_exporter_service:
  service.dead:
    - name: prometheus-postgres_exporter
    - enable: False

# Workaround for previous tomcat configuration
remove_tomcat_previous:
  file.rename:
    - source: /etc/sysconfig/tomcat
    - name: /etc/sysconfig/tomcat.bak
    - force: True
    - onlyif: test -f /etc/sysconfig/tomcat

jmx_tomcat_config:
  file.absent:
    - name: /etc/sysconfig/tomcat/systemd/jmx.conf
  mgrcompat.module_run:
    - name: service.systemctl_reload

jmx_taskomatic_config:
  file.absent:
    - name: /etc/sysconfig/taskomatic/systemd/jmx.conf
  mgrcompat.module_run:
    - name: service.systemctl_reload

mgr_enable_prometheus_self_monitoring:
  cmd.run:
    - name: command -p grep -q '^prometheus_monitoring_enabled.*=.*' /etc/rhn/rhn.conf && command -p sed -i 's/^prometheus_monitoring_enabled.*/prometheus_monitoring_enabled = 0/' /etc/rhn/rhn.conf || command -p echo 'prometheus_monitoring_enabled = 0' >> /etc/rhn/rhn.conf

mgr_is_prometheus_self_monitoring_disabled:
  cmd.run:
    - name: command -p grep -qF 'prometheus_monitoring_enabled = 0' /etc/rhn/rhn.conf
