# SPDX-FileCopyrightText: 2026 SUSE LLC
#
# SPDX-License-Identifier: Apache-2.0

node_exporter_service:
  module.run:
    - name: service.status
    - m_name: "prometheus-node_exporter.service"

postgres_exporter_service:
  module.run:
    - name: service.status
    - m_name: "prometheus-postgres_exporter.service"

jmx_tomcat_java_config:
  module.run:
    - name: file.search
    - path: /etc/tomcat/conf.d/tomcat_jmx.conf
    - pattern: "jmx_prometheus_javaagent.jar=5556"

jmx_taskomatic_java_config:
  module.run:
    - name: file.search
    - path: /etc/rhn/taskomatic.conf.d/taskomatic_jmx.conf
    - pattern: "jmx_prometheus_javaagent.jar=5557"

mgr_is_prometheus_self_monitoring_enabled:
  cmd.run:
    - name: /usr/bin/grep -q -E 'prometheus_monitoring_enabled\s*=\s*(1|y|true|yes|on)\s*$' /etc/rhn/rhn.conf

include:
  - util.syncstates
