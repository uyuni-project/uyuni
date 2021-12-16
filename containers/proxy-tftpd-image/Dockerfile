#!BuildTag: proxy-tftpd:latest proxy-tftpd:%PKG_VERSION% proxy-tftpd:%PKG_VERSION%.%RELEASE%

# HACK: Open Build Service currently only supports short names. Assume short name by default, allow to override to qualified name via ARG
ARG BASE_IMAGE_PREFIX=
FROM ${BASE_IMAGE_PREFIX}suse/sle15:15.3

# Distro repos
RUN zypper addrepo http://download.opensuse.org/distribution/leap/15.3/repo/oss/ main
RUN zypper addrepo http://download.opensuse.org/update/leap/15.3/sle/ updates

# Product repos
RUN zypper addrepo https://download.opensuse.org/repositories/systemsmanagement:/Uyuni:/Master/images/repo/Uyuni-Proxy-POOL-x86_64-Media1/ product

# Build Service required labels
# labelprefix=org.opensuse.uyuni.proxy-tftpd
LABEL org.opencontainers.image.title="Proxy salt-broker container"
LABEL org.opencontainers.image.description="Image contains a Proxy component to serve tftp requests"
LABEL org.opensuse.reference="registry.opensuse.org/proxy-tftpd:%PKG_VERSION%.%RELEASE%"
LABEL org.openbuildservice.disturl="%DISTURL%"
LABEL org.opencontainers.image.created="%BUILDTIME%"
# endlabelprefix

# tftp
EXPOSE 69/udp

VOLUME "/etc/uyuni"

# TODO: add installation

# TODO: add RUN directive
