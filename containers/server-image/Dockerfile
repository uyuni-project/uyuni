# SPDX-License-Identifier: MIT
#!BuildTag: uyuni/server:latest uyuni/server:4.4.0 uyuni/server:4.4.0.%RELEASE%

ARG INIT_BASE=registry.suse.com/bci/bci-base:15.4
FROM $INIT_BASE

ARG PRODUCT_REPO
ARG PRODUCT_PATTERN_PREFIX="patterns-uyuni"

# Add distro and product repos
COPY add_repos.sh /usr/bin
RUN sh add_repos.sh ${PRODUCT_REPO}

# Copy Uyuni setup script
COPY uyuni-setup.service /usr/lib/systemd/system/

# Main packages
RUN zypper ref && zypper --non-interactive up
RUN zypper --gpg-auto-import-keys --non-interactive install --auto-agree-with-licenses --force-resolution \
    ${PRODUCT_PATTERN_PREFIX}_server \
    ${PRODUCT_PATTERN_PREFIX}_retail \
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
    virtual-host-gatherer-Kubernetes \
    virtual-host-gatherer-libcloud \
    virtual-host-gatherer-Libvirt \
    virtual-host-gatherer-Nutanix \
    virtual-host-gatherer-VMware \
    vim


# FIXME hack to correct the report db script to work on containers
RUN cp /usr/bin/uyuni-setup-reportdb /usr/bin/uyuni-setup-reportdb.original
RUN sed -i 's/sysctl kernel.shmmax/#sysctl kernel.shmmax/g' /usr/bin/uyuni-setup-reportdb

RUN systemctl enable uyuni-setup

# LABELs
ARG PRODUCT=Uyuni
ARG VENDOR="Uyuni project"
ARG URL="https://www.uyuni-project.org/"
ARG REFERENCE_PREFIX="registry.opensuse.org/uyuni"

# Build Service required labels
# labelprefix=org.opensuse.uyuni.server
LABEL org.opencontainers.image.title="${PRODUCT} server container"
LABEL org.opencontainers.image.description="All-in-one ${PRODUCT} server image"
LABEL org.opencontainers.image.created="%BUILDTIME%"
LABEL org.opencontainers.image.vendor="${VENDOR}"
LABEL org.opencontainers.image.url="${URL}"
LABEL org.opencontainers.image.version="4.4.0"
LABEL org.openbuildservice.disturl="%DISTURL%"
LABEL org.opensuse.reference="${REFERENCE_PREFIX}/server:4.4.0.%RELEASE%"
# endlabelprefix

CMD ["/usr/lib/systemd/systemd"]
HEALTHCHECK --interval=5s --timeout=5s --retries=5 CMD ["/usr/bin/systemctl", "is-active", "multi-user.target"]
