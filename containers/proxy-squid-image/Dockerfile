#!BuildTag: proxy-squid:latest proxy-squid:%PKG_VERSION% proxy-squid:%PKG_VERSION%.%RELEASE%

FROM registry.suse.com/suse/sle15:15.3

ARG PRODUCT_REPO

# Add distro and product repos
COPY add_repos.sh /usr/bin
RUN sh add_repos.sh ${PRODUCT_REPO}

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
