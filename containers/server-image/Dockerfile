# SPDX-License-Identifier: MIT
#!BuildTag: uyuni/server:latest

ARG INIT_IMAGE=uyuni/init:latest
FROM $INIT_IMAGE

ARG PRODUCT_PATTERN_PREFIX="patterns-uyuni"

# Extra packages can be added via project configuration
ARG EXTRAPACKAGES

# Main packages
RUN echo "rpm.install.excludedocs = yes" >>/etc/zypp/zypp.conf && \
    zypper ref && zypper --non-interactive up && \
    zypper --gpg-auto-import-keys --non-interactive install --auto-agree-with-licenses --force-resolution \
        ${EXTRAPACKAGES} \
        ${PRODUCT_PATTERN_PREFIX}_server \
        ${PRODUCT_PATTERN_PREFIX}_retail \
        spacewalk-backend-sql-postgresql \
        spacewalk-java-postgresql \
        postgresql16-contrib \
        postgresql16-server \
        postgresql16 \
        billing-data-service \
        grub2-x86_64-efi \
        grub2-arm64-efi \
        grub2-powerpc-ieee1275 \
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
        saphanabootstrap-formula \
        sapnwbootstrap-formula \
        drbd-formula \
        habootstrap-formula \
        saline-formula \
        salt-shaptools \
        inter-server-sync \
        golang-github-prometheus-node_exporter \
        prometheus-postgres_exporter \
        golang-github-QubitProducts-exporter_exporter \
        prometheus-jmx_exporter \
        spacecmd \
        javamail \
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
        sssd-ad \
        sssd-ipa \
        sssd-krb5 \
        sssd-tools && \
    systemctl enable prometheus-node_exporter && \
    systemctl enable sssd

ADD root.tar.gz /

# Initialize environments to sync configuration and package files to persistent volumes
RUN systemctl enable timezone_alignment && \
    uyuni-configfiles-sync init /etc/apache2/ && \
    uyuni-configfiles-sync init /etc/cobbler/ && \
    uyuni-configfiles-sync init /etc/postfix/ && \
    uyuni-configfiles-sync init /etc/rhn/ && \
    uyuni-configfiles-sync init /etc/salt/ && \
    uyuni-configfiles-sync init /etc/sysconfig/ && \
    uyuni-configfiles-sync init /etc/tomcat/ && \
    uyuni-configfiles-sync init /srv/tftpboot/ && \
    uyuni-configfiles-sync init /srv/www/ && \
    uyuni-configfiles-sync init /var/lib/cobbler/

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
LABEL org.opencontainers.image.version=5.1.3
LABEL org.openbuildservice.disturl="%DISTURL%"
LABEL org.opensuse.reference="${REFERENCE_PREFIX}/server:${PRODUCT_VERSION}.%RELEASE%"
# endlabelprefix
LABEL org.uyuni.version="${PRODUCT_VERSION}"

CMD ["/usr/lib/systemd/systemd"]
HEALTHCHECK --interval=60s --timeout=120s --retries=5 --start-period=300s CMD ["/usr/bin/healthcheck.sh"]
