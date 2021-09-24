node_exporter_service:
  service.dead:
    - name: prometheus-node_exporter
    - enable: False

postgres_exporter_service:
  service.dead:
    - name: prometheus-postgres_exporter
    - enable: False

{% set remove_javaagent_props = {'service': 'tomcat', 'file': '/etc/sysconfig/tomcat'} %}
{%- include 'srvmonitoring/removejavaagentprops.sls' %}

jmx_tomcat_config:
  cmd.run:
    - name: grep -q -v -- 'jmx_prometheus_javaagent.jar' /etc/sysconfig/tomcat
    - require:
      - cmd: remove_tomcat_javaagent

{% set remove_javaagent_props = {'service': 'taskomatic', 'file': '/etc/rhn/taskomatic.conf'} %}
{%- include 'srvmonitoring/removejavaagentprops.sls' %}

jmx_taskomatic_config:
  cmd.run:
    - name: grep -q -v -- 'jmx_prometheus_javaagent.jar' /etc/rhn/taskomatic.conf
    - require:
      - cmd: remove_taskomatic_javaagent

mgr_enable_prometheus_self_monitoring:
  cmd.run:
    - name: grep -q '^prometheus_monitoring_enabled.*=.*' /etc/rhn/rhn.conf && sed -i 's/^prometheus_monitoring_enabled.*/prometheus_monitoring_enabled = 0/' /etc/rhn/rhn.conf || echo 'prometheus_monitoring_enabled = 0' >> /etc/rhn/rhn.conf

mgr_is_prometheus_self_monitoring_disabled:
  cmd.run:
    - name: grep -qF 'prometheus_monitoring_enabled = 0' /etc/rhn/rhn.conf
