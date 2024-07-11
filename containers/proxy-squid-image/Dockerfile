# SPDX-License-Identifier: MIT
#!BuildTag: uyuni/proxy-squid:latest

ARG BASE=registry.suse.com/bci/bci-base:15.5
FROM $BASE AS base

ARG PRODUCT_REPO

# Add distro and product repos
COPY add_repos.sh /usr/bin
RUN sh add_repos.sh ${PRODUCT_REPO}

# Main packages
COPY remove_unused.sh .
RUN echo "rpm.install.excludedocs = yes" >>/etc/zypp/zypp.conf
RUN zypper --gpg-auto-import-keys --non-interactive install --auto-agree-with-licenses \
    python3 \
    python3-PyYAML \
    squid && \
    sh remove_unused.sh

# Additional material
COPY uyuni-configure.py /usr/bin/uyuni-configure.py
RUN chmod +x /usr/bin/uyuni-configure.py

COPY squid.conf /etc/squid/squid.conf
# Ensure every run / exec uses the squid user to avoid errors like
#    FATAL: Cannot open '/proc/self/fd/1' for writing.
# For this we need to give the squid user access to squid.conf and squid.pid
RUN chown squid:squid /etc/squid/squid.conf
RUN mkdir -p /run/squid && chown squid:squid /run/squid

RUN mkdir -p /etc/squid/conf.d
COPY squid_example.conf /etc/squid/conf.d/squid_example.conf

# Ensure the cache is owned by squid user
RUN chown squid:squid /var/cache/squid
RUN chmod a+x /var/log

# Define slim image
ARG BASE=registry.suse.com/bci/bci-base:15.5
FROM $BASE AS slim

USER squid

ARG PRODUCT=Uyuni
ARG VENDOR="Uyuni project"
ARG URL="https://www.uyuni-project.org/"
ARG REFERENCE_PREFIX="registry.opensuse.org/uyuni"

COPY --from=base / /

# Build Service required labels
# labelprefix=org.opensuse.uyuni.proxy-squid
LABEL org.opencontainers.image.title="${PRODUCT} proxy squid container"
LABEL org.opencontainers.image.description="Image contains a ${PRODUCT} proxy component cache http requests"
LABEL org.opencontainers.image.created="%BUILDTIME%"
LABEL org.opencontainers.image.vendor="${VENDOR}"
LABEL org.opencontainers.image.url="${URL}"
LABEL org.opencontainers.image.name=proxy-squid-image
LABEL org.opencontainers.image.version=5.1.0
LABEL org.openbuildservice.disturl="%DISTURL%"
LABEL org.opensuse.reference="${REFERENCE_PREFIX}/proxy-squid:${PRODUCT_VERSION}.%RELEASE%"
# endlabelprefix
LABEL org.uyuni.version="${PRODUCT_VERSION}"

# Squid
EXPOSE 8080/tcp

VOLUME [ "/etc/uyuni", "/var/cache/squid" ]

CMD uyuni-configure.py && squid -z --foreground && squid -FC -d 1 --foreground
