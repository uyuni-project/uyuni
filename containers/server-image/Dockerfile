# SPDX-License-Identifier: MIT
#!BuildTag: uyuni/server:latest

ARG INIT_IMAGE=uyuni/init:latest
FROM $INIT_IMAGE

ARG PRODUCT_PATTERN_PREFIX="patterns-uyuni"

# Extra packages can be added via project configuration
ARG EXTRAPACKAGES

COPY timezone_alignment.sh /usr/bin

COPY healthcheck.sh /usr/bin

# Copy timezone link update service
COPY timezone_alignment.service /usr/lib/systemd/system/

COPY remove_unused.sh .
RUN echo "rpm.install.excludedocs = yes" >>/etc/zypp/zypp.conf

# Main packages
RUN zypper ref && zypper --non-interactive up
RUN zypper --gpg-auto-import-keys --non-interactive install --auto-agree-with-licenses --force-resolution \
    ${EXTRAPACKAGES} \
    ${PRODUCT_PATTERN_PREFIX}_server \
    ${PRODUCT_PATTERN_PREFIX}_retail \
    spacewalk-utils-extras \
    grub2-x86_64-efi \
    ed \
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
    virtualization-formulas \
    uyuni-config-formula \
    inter-server-sync \
    golang-github-lusitaniae-apache_exporter \
    golang-github-prometheus-node_exporter \
    prometheus-postgres_exporter \
    golang-github-QubitProducts-exporter_exporter \
    prometheus-jmx_exporter \
    spacecmd \
    javamail \
    libyui-ncurses-pkg16 \
    virtual-host-gatherer-libcloud \
    virtual-host-gatherer-Libvirt \
    virtual-host-gatherer-Nutanix \
    virtual-host-gatherer-VMware \
    vim \
    python3-pygit2 \
    ipmitool \
    sssd \
    sssd-dbus \
    sssd-ldap \
    nss-pam-ldapd \
    authselect

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
COPY pam_susemanager /etc/pam.d/susemanager

RUN chmod -R 755 /usr/bin/timezone_alignment.sh

# Set a warning message for interactive shells
COPY motd /etc/motd
RUN echo "cat /etc/motd" >/etc/sh.shrc.local

RUN pam-config -a --sss
RUN pam-config -a --ldap
RUN authselect select sssd --force
RUN authselect apply-changes

RUN systemctl enable prometheus-node_exporter; \
    systemctl enable uyuni-setup; \
    systemctl enable timezone_alignment; \
    systemctl enable sssd;

# Provide tool to synchronize package and configuration files to persistent volumes
COPY uyuni-configfiles-sync /usr/bin
RUN chmod -R 755 /usr/bin/uyuni-configfiles-sync

# Initialize environments to sync configuration and package files to persistent volumes
RUN uyuni-configfiles-sync init /etc/apache2/
RUN uyuni-configfiles-sync init /etc/cobbler/
RUN uyuni-configfiles-sync init /etc/postfix/
RUN uyuni-configfiles-sync init /etc/rhn/
RUN uyuni-configfiles-sync init /etc/salt/
RUN uyuni-configfiles-sync init /etc/sysconfig/
RUN uyuni-configfiles-sync init /etc/tomcat/
RUN uyuni-configfiles-sync init /srv/tftpboot/
RUN uyuni-configfiles-sync init /srv/www/
RUN uyuni-configfiles-sync init /var/lib/cobbler/

# LABELs
ARG PRODUCT=Uyuni
ARG VENDOR="Uyuni project"
ARG URL="https://www.uyuni-project.org/"
ARG REFERENCE_PREFIX="registry.opensuse.org/uyuni"

# Build Service required labels
# labelprefix=org.opensuse.uyuni.server
LABEL org.opencontainers.image.name=server-image
LABEL org.opencontainers.image.title="${PRODUCT} server container"
LABEL org.opencontainers.image.description="All-in-one ${PRODUCT} server image"
LABEL org.opencontainers.image.created="%BUILDTIME%"
LABEL org.opencontainers.image.vendor="${VENDOR}"
LABEL org.opencontainers.image.url="${URL}"
LABEL org.opencontainers.image.version=5.0.4
LABEL org.openbuildservice.disturl="%DISTURL%"
LABEL org.opensuse.reference="${REFERENCE_PREFIX}/server:${PRODUCT_VERSION}.%RELEASE%"
# endlabelprefix
LABEL org.uyuni.version="${PRODUCT_VERSION}"

CMD ["/usr/lib/systemd/systemd"]
HEALTHCHECK --interval=60s --timeout=120s --retries=5 --start-period=300s CMD ["/usr/bin/healthcheck.sh"]
