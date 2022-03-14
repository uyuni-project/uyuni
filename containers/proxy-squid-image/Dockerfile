#!BuildTag: uyuni/proxy-squid:latest uyuni/proxy-squid:%PKG_VERSION% uyuni/proxy-squid:%PKG_VERSION%.%RELEASE% uyuni/proxy-squid:beta

ARG BASE=registry.suse.com/bci/bci-base:15.3
FROM $BASE

ARG PRODUCT_REPO

# Add distro and product repos
COPY add_repos.sh /usr/bin
RUN sh add_repos.sh ${PRODUCT_REPO}

# Build Service required labels
# labelprefix=org.opensuse.uyuni.proxy-squid
LABEL org.opencontainers.image.title="Proxy squid container"
LABEL org.opencontainers.image.description="Image contains a Proxy component cache http requests"
LABEL org.opencontainers.image.created="%BUILDTIME%"
LABEL org.opencontainers.image.vendor="Uyuni project"
LABEL org.opencontainers.image.url="https://www.uyuni-project.org/"
LABEL org.opencontainers.image.version="%%PKG_VERSION%%"
LABEL org.openbuildservice.disturl="%DISTURL%"
LABEL org.opensuse.reference="registry.opensuse.org/uyuni/proxy-squid:%PKG_VERSION%.%RELEASE%"
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
