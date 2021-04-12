node_exporter_service:
  service.dead:
    - name: prometheus-node_exporter
    - enable: False

postgres_exporter_service:
  service.dead:
    - name: prometheus-postgres_exporter
    - enable: False

{% set remove_jmx_props = {'service': 'tomcat', 'file': '/etc/sysconfig/tomcat'} %}
{%- include 'srvmonitoring/removejmxprops.sls' %}

jmx_tomcat_config:
  cmd.run:
    - name: grep -q -v -- '-Dcom.sun.management.jmxremote.host=' /etc/sysconfig/tomcat && grep -q -v -- '-Dcom.sun.management.jmxremote.port=3333' /etc/sysconfig/tomcat && grep -q -v -- '-Dcom.sun.management.jmxremote.ssl=false' /etc/sysconfig/tomcat && grep -q -v -- '-Dcom.sun.management.jmxremote.authenticate=false' /etc/sysconfig/tomcat && grep -q -v -- '-Djava.rmi.server.hostname=' /etc/sysconfig/tomcat
    - require:
      - cmd: remove_tomcat_jmx_*

jmx_exporter_tomcat_service:
  service.dead:
    - name: prometheus-jmx_exporter@tomcat
    - enable: False
    - require:
      - cmd: jmx_tomcat_config

{% set remove_jmx_props = {'service': 'taskomatic', 'file': '/etc/rhn/taskomatic.conf'} %}
{%- include 'srvmonitoring/removejmxprops.sls' %}

jmx_taskomatic_config:
  cmd.run:
    - name: grep -q -v -- '-Dcom.sun.management.jmxremote.host=' /etc/rhn/taskomatic.conf && grep -q -v -- '-Dcom.sun.management.jmxremote.port=3334' /etc/rhn/taskomatic.conf && grep -q -v -- '-Dcom.sun.management.jmxremote.ssl=false' /etc/rhn/taskomatic.conf && grep -q -v -- '-Dcom.sun.management.jmxremote.authenticate=false' /etc/rhn/taskomatic.conf && grep -q -v -- '-Djava.rmi.server.hostname=' /etc/rhn/taskomatic.conf
    - require:
      - cmd: remove_taskomatic_jmx_*

jmx_exporter_taskomatic_service:
  service.dead:
    - name: prometheus-jmx_exporter@taskomatic
    - enable: False
    - require:
      - cmd: jmx_taskomatic_config

mgr_enable_prometheus_self_monitoring:
  cmd.run:
    - name: grep -q '^prometheus_monitoring_enabled.*=.*' /etc/rhn/rhn.conf && sed -i 's/^prometheus_monitoring_enabled.*/prometheus_monitoring_enabled = 0/' /etc/rhn/rhn.conf || echo 'prometheus_monitoring_enabled = 0' >> /etc/rhn/rhn.conf

mgr_is_prometheus_self_monitoring_disabled:
  cmd.run:
    - name: grep -qF 'prometheus_monitoring_enabled = 0' /etc/rhn/rhn.conf
