#!BuildTag: uyuni/proxy-tftpd:latest uyuni/proxy-tftpd:%PKG_VERSION% uyuni/proxy-tftpd:%PKG_VERSION%.%RELEASE% uyuni/proxy-tftpd:beta

ARG BASE=registry.suse.com/bci/bci-base:15.3
FROM $BASE

ARG PRODUCT_REPO
ARG PRODUCT=Uyuni
ARG VENDOR="Uyuni project"
ARG URL="https://www.uyuni-project.org/"
ARG REFERENCE_PREFIX="registry.opensuse.org/uyuni"

# Add distro and product repos
COPY add_repos.sh /usr/bin
RUN sh add_repos.sh ${PRODUCT_REPO}

# Build Service required labels
# labelprefix=org.opensuse.uyuni.proxy-tftpd
LABEL org.opencontainers.image.title="${PRODUCT} proxy tftpd container"
LABEL org.opencontainers.image.description="Image contains a ${PRODUCT} proxy component to serve tftp requests"
LABEL org.opencontainers.image.created="%BUILDTIME%"
LABEL org.opencontainers.image.vendor="${VENDOR}"
LABEL org.opencontainers.image.url="${URL}"
LABEL org.opencontainers.image.version="%%PKG_VERSION%%"
LABEL org.openbuildservice.disturl="%DISTURL%"
LABEL org.opensuse.reference="${REFERENCE_PREFIX}/proxy-tftpd:%PKG_VERSION%.%RELEASE%"
# endlabelprefix

# tftp
EXPOSE 69/udp

VOLUME [ "/etc/uyuni", "/srv/tftpboot" ]

RUN zypper --gpg-auto-import-keys --non-interactive install --auto-agree-with-licenses tftp python3-PyYAML

COPY uyuni-configure.py /usr/bin/uyuni-configure.py
RUN chmod +x /usr/bin/uyuni-configure.py

CMD uyuni-configure.py && ( source /etc/sysconfig/tftp; /usr/sbin/in.tftpd -L -u "$TFTP_USER" -s "$TFTP_DIRECTORY" $TFTP_OPTIONS )
