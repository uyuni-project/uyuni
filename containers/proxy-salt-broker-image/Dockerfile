#!BuildTag: proxy-salt-broker:latest proxy-salt-broker:%PKG_VERSION% proxy-salt-broker:%PKG_VERSION%.%RELEASE%

# HACK: Open Build Service currently only supports short names. Assume short name by default, allow to override to qualified name via ARG
ARG BASE_IMAGE_PREFIX=
FROM ${BASE_IMAGE_PREFIX}suse/sle15:15.3

# Distro repos
RUN zypper addrepo http://download.opensuse.org/distribution/leap/15.3/repo/oss/ main
RUN zypper addrepo http://download.opensuse.org/update/leap/15.3/sle/ updates

# Product repos
RUN zypper addrepo https://download.opensuse.org/repositories/systemsmanagement:/Uyuni:/Master/images/repo/Uyuni-Proxy-POOL-x86_64-Media1/ product

# Build Service required labels
# labelprefix=org.opensuse.uyuni.proxy-salt-broker
LABEL org.opencontainers.image.title="Proxy salt-broker container"
LABEL org.opencontainers.image.description="Image contains a Proxy component to forward Salt connections"
LABEL org.opensuse.reference="registry.opensuse.org/proxy-salt-broker:%PKG_VERSION%.%RELEASE%"
LABEL org.openbuildservice.disturl="%DISTURL%"
LABEL org.opencontainers.image.created="%BUILDTIME%"
# endlabelprefix

# Salt
EXPOSE 4505/tcp
EXPOSE 4506/tcp

VOLUME "/etc/uyuni"

RUN zypper --gpg-auto-import-keys --non-interactive install --auto-agree-with-licenses spacewalk-proxy-salt

# TODO -> remove the following block of lines after the merge
COPY proxy/proxy/salt-broker/salt-broker /usr/bin/salt-broker
RUN chmod +x /usr/bin/salt-broker
COPY proxy/proxy/salt-broker/broker /etc/salt/broker

COPY containers/proxy-salt-broker-image/uyuni-configure.py /usr/bin/uyuni-configure.py
RUN chmod +x /usr/bin/uyuni-configure.py

CMD uyuni-configure.py && salt-broker
