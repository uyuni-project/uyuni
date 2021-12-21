#!BuildTag: proxy-squid:latest proxy-squid:%PKG_VERSION% proxy-squid:%PKG_VERSION%.%RELEASE%

# HACK: Open Build Service currently only supports short names. Assume short name by default, allow to override to qualified name via ARG
ARG BASE_IMAGE_PREFIX=
FROM ${BASE_IMAGE_PREFIX}suse/sle15:15.3

# Distro repos
RUN zypper addrepo http://download.opensuse.org/distribution/leap/15.3/repo/oss/ main
RUN zypper addrepo http://download.opensuse.org/update/leap/15.3/sle/ updates

# Product repos
RUN zypper addrepo https://download.opensuse.org/repositories/systemsmanagement:/Uyuni:/Master/images/repo/Uyuni-Proxy-POOL-x86_64-Media1/ product

# Build Service required labels
# labelprefix=org.opensuse.uyuni.proxy-squid
LABEL org.opencontainers.image.title="Proxy squid container"
LABEL org.opencontainers.image.description="Image contains a Proxy component cache http requests"
LABEL org.opensuse.reference="registry.opensuse.org/proxy-squid:%PKG_VERSION%.%RELEASE%"
LABEL org.openbuildservice.disturl="%DISTURL%"
LABEL org.opencontainers.image.created="%BUILDTIME%"
# endlabelprefix

# Squid
EXPOSE 8080/tcp

VOLUME [ "/etc/uyuni", "/var/cache/squid" ]

RUN zypper --gpg-auto-import-keys --non-interactive install --auto-agree-with-licenses python3 python3-PyYAML
RUN zypper --gpg-auto-import-keys --non-interactive install --auto-agree-with-licenses squid

COPY squid.conf /etc/squid/squid.conf

COPY uyuni-configure.py /usr/bin/uyuni-configure.py
RUN chmod +x /usr/bin/uyuni-configure.py

CMD uyuni-configure.py && squid -z --foreground && squid -FC -d 1 --foreground
