#!/usr/bin/env bash

set -xe

CONFIG_SOURCE="/usr/local/config"

# Postgres exporter service configuration
mkdir /etc/postgres_exporter
mv ${CONFIG_SOURCE}/postgres_exporter_queries.yaml /etc/postgres_exporter/postgres_exporter_queries.yaml
mv ${CONFIG_SOURCE}/postgres-exporter /etc/sysconfig/prometheus-postgres_exporter

# jmx exporter tomcat configuration:
mkdir -p /etc/prometheus-jmx_exporter/tomcat
mkdir -p /usr/lib/systemd/system/tomcat.service.d
cp ${CONFIG_SOURCE}/java_agent.yaml /etc/prometheus-jmx_exporter/tomcat/java_agent.yml
mv ${CONFIG_SOURCE}/tomcat_jmx.conf /usr/lib/systemd/system/tomcat.service.d/jmx.conf

# jmx_exporter taskomatic configuration:
mkdir -p /etc/prometheus-jmx_exporter/taskomatic
mkdir -p /usr/lib/systemd/system/taskomatic.service.d
mv ${CONFIG_SOURCE}/java_agent.yaml /etc/prometheus-jmx_exporter/taskomatic/java_agent.yml
mv ${CONFIG_SOURCE}/taskomatic_jmx.conf /usr/lib/systemd/system/taskomatic.service.d/jmx.conf

systemctl daemon-reload
systemctl enable node_exporter_service
systemctl enable prometheus-node_exporter
