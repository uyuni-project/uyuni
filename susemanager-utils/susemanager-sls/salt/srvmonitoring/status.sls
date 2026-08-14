node_exporter_service:
  module.run:
    - name: service.status
    - m_name: "prometheus-node_exporter.service"

postgres_exporter_service:
  module.run:
    - name: service.status
    - m_name: "prometheus-postgres_exporter.service"

# Migrate legacy JMX configs from systemd drop-ins to conf.d files.
# After upgrade, old configs may still exist at the old paths while
# the new paths don't exist yet. Copy content to the new location.
migrate_tomcat_jmx_config:
  cmd.run:
    - name: >-
        if [ -f /etc/sysconfig/tomcat/systemd/jmx.conf ] && [ ! -f /etc/tomcat/conf.d/tomcat_jmx.conf ]; then
          mkdir -p /etc/tomcat/conf.d;
          cp /usr/share/susemanager/salt/srvmonitoring/tomcat_jmx.conf /etc/tomcat/conf.d/tomcat_jmx.conf;
        fi
    - onlyif: test -f /etc/sysconfig/tomcat/systemd/jmx.conf -a ! -f /etc/tomcat/conf.d/tomcat_jmx.conf

legacy_tomcat_sysconfig_jmx_cleanup:
  file.absent:
    - name: /etc/sysconfig/tomcat/systemd/jmx.conf
    - require:
      - cmd: migrate_tomcat_jmx_config

legacy_tomcat_systemd_dropin_jmx_cleanup:
  file.absent:
    - name: /usr/lib/systemd/system/tomcat.service.d/jmx.conf
    - require:
      - cmd: migrate_tomcat_jmx_config

migrate_taskomatic_jmx_config:
  cmd.run:
    - name: >-
        if [ -f /etc/sysconfig/taskomatic/systemd/jmx.conf ] && [ ! -f /etc/rhn/taskomatic.conf.d/taskomatic_jmx.conf ]; then
          mkdir -p /etc/rhn/taskomatic.conf.d;
          cp /usr/share/susemanager/salt/srvmonitoring/taskomatic_jmx.conf /etc/rhn/taskomatic.conf.d/taskomatic_jmx.conf;
        fi
    - onlyif: test -f /etc/sysconfig/taskomatic/systemd/jmx.conf -a ! -f /etc/rhn/taskomatic.conf.d/taskomatic_jmx.conf

legacy_taskomatic_sysconfig_jmx_cleanup:
  file.absent:
    - name: /etc/sysconfig/taskomatic/systemd/jmx.conf
    - require:
      - cmd: migrate_taskomatic_jmx_config

legacy_taskomatic_systemd_dropin_jmx_cleanup:
  file.absent:
    - name: /usr/lib/systemd/system/taskomatic.service.d/jmx.conf
    - require:
      - cmd: migrate_taskomatic_jmx_config

jmx_tomcat_java_config:
  module.run:
    - name: file.search
    - path: /etc/tomcat/conf.d/tomcat_jmx.conf
    - pattern: "jmx_prometheus_javaagent.jar=5556"
    - require:
      - cmd: migrate_tomcat_jmx_config

jmx_taskomatic_java_config:
  module.run:
    - name: file.search
    - path: /etc/rhn/taskomatic.conf.d/taskomatic_jmx.conf
    - pattern: "jmx_prometheus_javaagent.jar=5557"
    - require:
      - cmd: migrate_taskomatic_jmx_config

mgr_is_prometheus_self_monitoring_enabled:
  cmd.run:
    - name: /usr/bin/grep -q -E 'prometheus_monitoring_enabled\s*=\s*(1|y|true|yes|on)\s*$' /etc/rhn/rhn.conf

include:
  - util.syncstates
