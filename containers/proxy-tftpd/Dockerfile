#!BuildTag: proxy-tftpd:latest proxy-tftpd:%PKG_VERSION% proxy-tftpd:%PKG_VERSION%.%RELEASE%

FROM suse/sle15:15.3

# Distro repos
RUN zypper addrepo http://download.opensuse.org/distribution/leap/15.3/repo/oss/ main
RUN zypper addrepo http://download.opensuse.org/update/leap/15.3/sle/ updates

# Product repos
RUN zypper addrepo https://download.opensuse.org/repositories/systemsmanagement:/Uyuni:/Master/images/repo/Uyuni-Proxy-POOL-x86_64-Media1/ product

# tftp
EXPOSE 69/udp

VOLUME "/etc/uyuni"

# TODO: add installation

# TODO: add RUN directive
