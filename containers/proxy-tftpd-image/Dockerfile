# SPDX-License-Identifier: MIT
#!BuildTag: uyuni/proxy-tftpd:latest

ARG BASE=registry.suse.com/suse/ltss/sle15.4/sle15:15.4
FROM $BASE AS base

ARG PRODUCT_REPO

# Add distro and product repos
COPY add_repos.sh /usr/bin
RUN sh add_repos.sh ${PRODUCT_REPO}

# Main packages
COPY remove_unused.sh .
RUN echo "rpm.install.excludedocs = yes" >>/etc/zypp/zypp.conf
RUN zypper --gpg-auto-import-keys --non-interactive install --auto-agree-with-licenses \
    python3-PyYAML \
    python3-fbtftp \
    python3-requests && \
    sh remove_unused.sh

# Additional material
COPY uyuni-configure.py /usr/bin/uyuni-configure.py
RUN chmod +x /usr/bin/uyuni-configure.py

COPY tftp_wrapper.py /usr/bin/tftp_wrapper.py
RUN chmod +x /usr/bin/tftp_wrapper.py

# Define slim image
ARG BASE=registry.suse.com/bci/bci-base:15.4
FROM $BASE AS slim

ARG PRODUCT=Uyuni
ARG VENDOR="Uyuni project"
ARG URL="https://www.uyuni-project.org/"
ARG REFERENCE_PREFIX="registry.opensuse.org/uyuni"

COPY --from=base / /

# Build Service required labels
# labelprefix=org.opensuse.uyuni.proxy-tftpd
LABEL org.opencontainers.image.title="${PRODUCT} proxy tftpd container"
LABEL org.opencontainers.image.description="Image contains a ${PRODUCT} proxy component to serve tftp requests"
LABEL org.opencontainers.image.created="%BUILDTIME%"
LABEL org.opencontainers.image.vendor="${VENDOR}"
LABEL org.opencontainers.image.url="${URL}"
LABEL org.opencontainers.image.name=proxy-tftpd-image
LABEL org.opencontainers.image.version=4.3.16
LABEL org.openbuildservice.disturl="%DISTURL%"
LABEL org.opensuse.reference="${REFERENCE_PREFIX}/proxy-tftpd:${PRODUCT_VERSION}.%RELEASE%"
# endlabelprefix
LABEL org.uyuni.version="${PRODUCT_VERSION}"

# tftp
EXPOSE 69/udp

VOLUME [ "/etc/uyuni", "/srv/tftpboot" ]

CMD uyuni-configure.py && /usr/bin/tftp_wrapper.py
