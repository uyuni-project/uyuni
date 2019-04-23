jmx_taskomatic_exporter_service:
  module.run:
    - name: service.status
    - m_name: "prometheus-jmx_exporter@taskomatic.service"

jmx_tomcat_exporter_service:
  module.run:
    - name: service.status
    - m_name: "prometheus-jmx_exporter@tomcat.service"

node_exporter_service:
  module.run:
    - name: service.status
    - m_name: "prometheus-node_exporter.service"

postgres_exporter_service:
  module.run:
    - name: service.status
    - m_name: "prometheus-postgres_exporter.service"

