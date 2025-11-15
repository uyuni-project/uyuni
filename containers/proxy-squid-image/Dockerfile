# SPDX-License-Identifier: MIT
#!BuildTag: uyuni/proxy-squid:latest

ARG BASE=registry.suse.com/bci/bci-base:15.7
FROM $BASE AS base

# Main packages
RUN zypper --gpg-auto-import-keys --non-interactive install --auto-agree-with-licenses \
        python3 \
        python3-PyYAML \
        squid && \
# Ensure every run / exec uses the squid user to avoid errors like
#    FATAL: Cannot open '/proc/self/fd/1' for writing.
# For this we need to give the squid user access to squid.conf and squid.pid
    mkdir -p /run/squid /etc/squid/conf.d && \
    chown squid:squid /run/squid && \
# Ensure the cache is owned by squid user
    chown squid:squid /var/cache/squid && \
    chmod a+x /var/log && \
    zypper --non-interactive clean --all && \
    rpm -e zypper && \
    rm -rf /var/log/{alternatives.log,lastlog,tallylog,suseconnect.log,zypper.log,zypp/history,YaST2}

# Additional material
COPY --chmod=555 uyuni-configure.py /usr/bin/uyuni-configure.py
COPY --chown=squid:squid squid.conf /etc/squid/squid.conf
COPY squid_example.conf /etc/squid/conf.d/squid_example.conf

# LABELs
ARG PRODUCT=Uyuni
ARG VENDOR="Uyuni project"
ARG URL="https://www.uyuni-project.org/"
ARG REFERENCE_PREFIX="registry.opensuse.org/uyuni"

# Build Service required labels
# labelprefix=org.opensuse.uyuni.proxy-squid
LABEL org.opencontainers.image.title="${PRODUCT} proxy squid container"
LABEL org.opencontainers.image.description="Image contains a ${PRODUCT} proxy component cache http requests"
LABEL org.opencontainers.image.created="%BUILDTIME%"
LABEL org.opencontainers.image.vendor="${VENDOR}"
LABEL org.opencontainers.image.url="${URL}"
LABEL org.opencontainers.image.name=proxy-squid-image
LABEL org.opencontainers.image.version=5.2.1
LABEL org.openbuildservice.disturl="%DISTURL%"
LABEL org.opensuse.reference="${REFERENCE_PREFIX}/proxy-squid:${PRODUCT_VERSION}.%RELEASE%"
# endlabelprefix
LABEL org.uyuni.version="${PRODUCT_VERSION}"

# Squid
EXPOSE 8080/tcp

VOLUME [ "/etc/uyuni", "/var/cache/squid" ]

USER squid
CMD uyuni-configure.py && squid -z --foreground && squid -FC -d 1 --foreground
