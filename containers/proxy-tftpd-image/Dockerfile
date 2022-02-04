#!BuildTag: proxy-tftpd:latest proxy-tftpd:%PKG_VERSION% proxy-tftpd:%PKG_VERSION%.%RELEASE%

FROM registry.suse.com/suse/sle15:15.3

ARG PRODUCT_REPO

# Add distro and product repos
COPY add_repos.sh /usr/bin
RUN sh add_repos.sh ${PRODUCT_REPO}

# Build Service required labels
# labelprefix=org.opensuse.uyuni.proxy-tftpd
LABEL org.opencontainers.image.title="Proxy tftpd container"
LABEL org.opencontainers.image.description="Image contains a Proxy component to serve tftp requests"
LABEL org.opensuse.reference="registry.opensuse.org/proxy-tftpd:%PKG_VERSION%.%RELEASE%"
LABEL org.openbuildservice.disturl="%DISTURL%"
LABEL org.opencontainers.image.created="%BUILDTIME%"
# endlabelprefix

# tftp
EXPOSE 69/udp

VOLUME "/etc/uyuni"

# TODO: check installation

RUN zypper --gpg-auto-import-keys --non-interactive install --auto-agree-with-licenses tftp

# TODO: add RUN directive
