# SPDX-License-Identifier: MIT
#!BuildTag: uyuni/server:latest

ARG INIT_IMAGE=uyuni/init:latest
FROM $INIT_IMAGE

ARG PRODUCT_PATTERN_PREFIX="patterns-uyuni"

# Keep in sync with salt/salt.spec
# For now Uyuni is still on python3-salt but the value can be overridden in the project config
ARG USEPYTHON="python3"

# Extra packages can be added via project configuration
ARG EXTRAPACKAGES

# Main packages
RUN echo "rpm.install.excludedocs = yes" >>/etc/zypp/zypp.conf && \
    zypper ref && zypper --non-interactive up && \
    zypper --gpg-auto-import-keys --non-interactive install --auto-agree-with-licenses --force-resolution \
        ${EXTRAPACKAGES} \
        ${PRODUCT_PATTERN_PREFIX}_server \
        ${PRODUCT_PATTERN_PREFIX}_retail \
        ${USEPYTHON}-pygit2 \
        ${USEPYTHON}-ldap \
        billing-data-service \
        drbd-formula \
        ed \
        golang-github-QubitProducts-exporter_exporter \
        golang-github-prometheus-node_exporter \
        grafana-formula \
        grub2-arm64-efi \
        grub2-powerpc-ieee1275 \
        grub2-x86_64-efi \
        habootstrap-formula \
        inter-server-sync \
        ipmitool \
        javamail \
        locale-formula \
        postgresql16 \
        prometheus-exporters-formula \
        prometheus-formula \
        prometheus-jmx_exporter \
        prometheus-jmx_exporter-tomcat \
        prometheus-postgres_exporter \
        registry-formula \
        saline-formula \
        salt-shaptools \
        saphanabootstrap-formula \
        sapnwbootstrap-formula \
        spacecmd \
        spacewalk-backend-sql-postgresql \
        spacewalk-java-postgresql \
        sssd \
        sssd-ad \
        sssd-dbus \
        sssd-ipa \
        sssd-krb5 \
        sssd-ldap \
        sssd-tools \
        uyuni-config-formula \
        vim \
        virtual-host-gatherer-Libvirt \
        virtual-host-gatherer-Nutanix \
        virtual-host-gatherer-VMware \
        virtual-host-gatherer-libcloud \
        virtualization-formulas \
        zchunk && \
    systemctl enable prometheus-node_exporter && \
    systemctl enable sssd && \
    rm /etc/krb5.conf.d/crypto-policies && \
    ln -s /etc/crypto-policies/back-ends/krb5.config /etc/krb5.conf.d/crypto-policies && \
    mv "/etc/krb5.conf.d" "/etc/rhn/krb5.conf.d" && \
    ln -s "/etc/rhn/krb5.conf.d" "/etc/krb5.conf.d"

RUN set -euo pipefail; zypper -n rr -a; \
    rm -rf /var/log/{alternatives.log,lastlog,tallylog,zypper.log,zypp/history,YaST2}

ADD --chown=root:root root.tar.gz /

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
LABEL org.opencontainers.image.version=5.2.1
LABEL org.openbuildservice.disturl="%DISTURL%"
LABEL org.opensuse.reference="${REFERENCE_PREFIX}/server:${PRODUCT_VERSION}.%RELEASE%"
# endlabelprefix
LABEL org.uyuni.version="${PRODUCT_VERSION}"

CMD ["/usr/lib/systemd/systemd"]
HEALTHCHECK --interval=60s --timeout=120s --retries=5 --start-period=300s CMD ["/usr/bin/healthcheck.sh"]
