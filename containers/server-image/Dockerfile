# SPDX-License-Identifier: MIT
#!BuildTag: uyuni/server:latest uyuni/server:%PKG_VERSION% uyuni/server:%PKG_VERSION%.%RELEASE%

ARG INIT_BASE=registry.suse.com/bci/bci-base:15.4
FROM $INIT_BASE

ARG PRODUCT_REPO
ARG PRODUCT_PATTERN="patterns-uyuni_server"

# Add distro and product repos
COPY add_repos.sh /usr/bin
RUN sh add_repos.sh ${PRODUCT_REPO}

# Copy Uyuni setup script
COPY uyuni-setup.service /usr/lib/systemd/system/

# Main packages
COPY remove_unused.sh .
RUN echo "rpm.install.excludedocs = yes" >>/etc/zypp/zypp.conf
# TODO Remove vi
RUN zypper ref
RUN zypper --gpg-auto-import-keys --non-interactive install --auto-agree-with-licenses --force-resolution \
    ${PRODUCT_PATTERN} \
    susemanager-tftpsync \
    golang-github-prometheus-node_exporter \
    prometheus-postgres_exporter \
    golang-github-QubitProducts-exporter_exporter \
    prometheus-jmx_exporter \
    prometheus-jmx_exporter-tomcat \
    spacecmd \
    grafana-formula \
    locale-formula \
    prometheus-exporters-formula \
    prometheus-formula \
    registry-formula \
    bind-formula \
    dhcpd-formula \
    tftpd-formula \
    virtualization-formulas \
    vsftpd-formula \
    branch-network-formula \
    pxe-formula \
    saltboot-formula \
    image-sync-formula \
    golang-github-lusitaniae-apache_exporter \
    golang-github-prometheus-node_exporter \
    prometheus-postgres_exporter \
    golang-github-QubitProducts-exporter_exporter \
    prometheus-jmx_exporter \
    spacecmd \
    python3-susemanager-retail \
    susemanager-retail-tools \
    javamail \
    libyui-ncurses-pkg16 \
    vim

RUN sed -i 's/sysctl kernel.shmmax/#sysctl kernel.shmmax/g' /usr/bin/uyuni-setup-reportdb

RUN mkdir -p /etc/postgres_exporter \
             /etc/prometheus-jmx_exporter/tomcat \
             /usr/lib/systemd/system/tomcat.service.d \
             /etc/prometheus-jmx_exporter/taskomatic \
             /usr/lib/systemd/system/taskomatic.service.d

COPY postgres_exporter_queries.yaml /etc/postgres_exporter/postgres_exporter_queries.yaml
COPY postgres-exporter /etc/sysconfig/prometheus-postgres_exporter
COPY java_agent.yaml /etc/prometheus-jmx_exporter/tomcat/java_agent.yml
COPY java_agent.yaml /etc/prometheus-jmx_exporter/taskomatic/java_agent.yml
COPY tomcat_jmx.conf /usr/lib/systemd/system/tomcat.service.d/jmx.conf
COPY taskomatic_jmx.conf /usr/lib/systemd/system/taskomatic.service.d/jmx.conf

RUN systemctl enable prometheus-node_exporter; \
    systemctl enable uyuni-setup

# LABELs
ARG PRODUCT=Uyuni
ARG VENDOR="Uyuni project"
ARG URL="https://www.uyuni-project.org/"
ARG REFERENCE_PREFIX="registry.opensuse.org/uyuni"

# Build Service required labels
# labelprefix=org.opensuse.uyuni.proxy-httpd
LABEL org.opencontainers.image.title="${PRODUCT} server container"
LABEL org.opencontainers.image.description="All-in-one ${PRODUCT} server image"
LABEL org.opencontainers.image.created="%BUILDTIME%"
LABEL org.opencontainers.image.vendor="${VENDOR}"
LABEL org.opencontainers.image.url="${URL}"
LABEL org.opencontainers.image.version="%PKG_VERSION%"
LABEL org.openbuildservice.disturl="%DISTURL%"
LABEL org.opensuse.reference="${REFERENCE_PREFIX}/server:%PKG_VERSION%.%RELEASE%"
# endlabelprefix

CMD ["/usr/lib/systemd/systemd"]
HEALTHCHECK --interval=5s --timeout=5s --retries=5 CMD ["/usr/bin/systemctl", "is-active", "multi-user.target"]
