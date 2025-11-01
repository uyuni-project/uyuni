# SPDX-License-Identifier: MIT
#!BuildTag: uyuni/proxy-tftpd:latest

ARG BASE=registry.suse.com/bci/bci-base:15.7
FROM $BASE AS base

ARG PRODUCT_REPO

# Main packages
RUN [ -n "$PRODUCT_REPO" ] && zypper addrepo $PRODUCT_REPO product; \
    zypper --gpg-auto-import-keys --non-interactive install --auto-agree-with-licenses \
        python3-PyYAML \
        python3-fbtftp \
        python3-requests && \
    zypper --non-interactive clean --all && \
    rm -rf /var/log/{alternatives.log,lastlog,tallylog,suseconnect.log,zypper.log,zypp/history,YaST2}

# Additional material
COPY --chmod=+x uyuni-configure.py /usr/bin/uyuni-configure.py
COPY --chmod=+x tftp_wrapper.py /usr/bin/tftp_wrapper.py

# LABELs
ARG PRODUCT=Uyuni
ARG VENDOR="Uyuni project"
ARG URL="https://www.uyuni-project.org/"
ARG REFERENCE_PREFIX="registry.opensuse.org/uyuni"

# Build Service required labels
# labelprefix=org.opensuse.uyuni.proxy-tftpd
LABEL org.opencontainers.image.title="${PRODUCT} proxy tftpd container"
LABEL org.opencontainers.image.description="Image contains a ${PRODUCT} proxy component to serve tftp requests"
LABEL org.opencontainers.image.created="%BUILDTIME%"
LABEL org.opencontainers.image.vendor="${VENDOR}"
LABEL org.opencontainers.image.url="${URL}"
LABEL org.opencontainers.image.name=proxy-tftpd-image
LABEL org.opencontainers.image.version=5.2.1
LABEL org.openbuildservice.disturl="%DISTURL%"
LABEL org.opensuse.reference="${REFERENCE_PREFIX}/proxy-tftpd:${PRODUCT_VERSION}.%RELEASE%"
# endlabelprefix
LABEL org.uyuni.version="${PRODUCT_VERSION}"

# tftp
EXPOSE 69/udp

VOLUME [ "/etc/uyuni", "/srv/tftpboot" ]

CMD uyuni-configure.py && /usr/bin/tftp_wrapper.py
