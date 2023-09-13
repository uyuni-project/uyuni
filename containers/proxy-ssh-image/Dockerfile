# SPDX-License-Identifier: MIT
#!BuildTag: uyuni/proxy-ssh:latest

ARG BASE=registry.suse.com/bci/bci-base:15.4
FROM $BASE AS base

ARG PRODUCT_REPO

# Add distro and product repos
COPY add_repos.sh /usr/bin
RUN sh add_repos.sh ${PRODUCT_REPO}

# Main packages
COPY remove_unused.sh .
RUN echo "rpm.install.excludedocs = yes" >>/etc/zypp/zypp.conf
RUN zypper --gpg-auto-import-keys --non-interactive install --auto-agree-with-licenses \
    openssh-server \
    openssh \
    python3 \
    python3-PyYAML && \
    sh remove_unused.sh

# Additional material
COPY mgr-proxy-ssh-force-cmd /usr/sbin/mgr-proxy-ssh-force-cmd
RUN chmod a+x /usr/sbin/mgr-proxy-ssh-force-cmd

COPY uyuni-configure.py /usr/bin/uyuni-configure.py
RUN chmod +x /usr/bin/uyuni-configure.py

# Define slim image
ARG BASE=registry.suse.com/bci/bci-base:15.4
FROM $BASE AS slim

ARG PRODUCT=Uyuni
ARG VENDOR="Uyuni project"
ARG URL="https://www.uyuni-project.org/"
ARG REFERENCE_PREFIX="registry.opensuse.org/uyuni"

COPY --from=base / /

# Build Service required labels
# labelprefix=org.opensuse.uyuni.proxy-ssh
LABEL org.opencontainers.image.title="${PRODUCT} proxy ssh container"
LABEL org.opencontainers.image.description="Image contains a ${PRODUCT} proxy component to serve and forward ssh access"
LABEL org.opencontainers.image.created="%BUILDTIME%"
LABEL org.opencontainers.image.vendor="${VENDOR}"
LABEL org.opencontainers.image.url="${URL}"
LABEL org.opencontainers.image.name=proxy-ssh-image
LABEL org.opencontainers.image.version=4.3.11
LABEL org.openbuildservice.disturl="%DISTURL%"
LABEL org.opensuse.reference="${REFERENCE_PREFIX}/proxy-ssh:${PRODUCT_VERSION}.%RELEASE%"
# endlabelprefix
LABEL org.uyuni.version="${PRODUCT_VERSION}"

# SSH port
EXPOSE 22

VOLUME "/etc/uyuni"

CMD uyuni-configure.py && /usr/sbin/sshd -D -e
