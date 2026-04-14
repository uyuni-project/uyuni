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
    - name: /etc/tomcat/conf.d/tomcat_jmx.conf

jmx_taskomatic_config:
  file.absent:
    - name: /etc/rhn/taskomatic.conf.d/taskomatic_jmx.conf

# Legacy systemd drop-in and sysconfig JMX config cleanup
legacy_tomcat_sysconfig_jmx_cleanup:
  file.absent:
    - name: /etc/sysconfig/tomcat/systemd/jmx.conf

legacy_tomcat_systemd_dropin_jmx_cleanup:
  file.absent:
    - name: /usr/lib/systemd/system/tomcat.service.d/jmx.conf

legacy_taskomatic_sysconfig_jmx_cleanup:
  file.absent:
    - name: /etc/sysconfig/taskomatic/systemd/jmx.conf

legacy_taskomatic_systemd_dropin_jmx_cleanup:
  file.absent:
    - name: /usr/lib/systemd/system/taskomatic.service.d/jmx.conf

mgr_enable_prometheus_self_monitoring:
  cmd.run:
    - name: command -p grep -q '^prometheus_monitoring_enabled.*=.*' /etc/rhn/rhn.conf && command -p sed -i 's/^prometheus_monitoring_enabled.*/prometheus_monitoring_enabled = 0/' /etc/rhn/rhn.conf || command -p echo 'prometheus_monitoring_enabled = 0' >> /etc/rhn/rhn.conf

mgr_is_prometheus_self_monitoring_disabled:
  cmd.run:
    - name: command -p grep -qF 'prometheus_monitoring_enabled = 0' /etc/rhn/rhn.conf
