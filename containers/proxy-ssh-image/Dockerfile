# SPDX-License-Identifier: MIT
#!BuildTag: uyuni/proxy-ssh:latest

ARG BASE=registry.suse.com/bci/bci-base:15.7
FROM $BASE AS base

# Main packages
RUN zypper --gpg-auto-import-keys --non-interactive install --auto-agree-with-licenses \
        openssh-server \
        openssh \
        python3 \
        python3-PyYAML && \
    zypper --non-interactive clean --all && \
    rpm -e zypper && \
    rm -rf /var/log/{alternatives.log,lastlog,tallylog,suseconnect.log,zypper.log,zypp/history,YaST2}

# Additional material
COPY --chmod=555 mgr-proxy-ssh-force-cmd /usr/sbin/mgr-proxy-ssh-force-cmd
COPY --chmod=555 uyuni-configure.py /usr/bin/uyuni-configure.py

# LABELs
ARG PRODUCT=Uyuni
ARG VENDOR="Uyuni project"
ARG URL="https://www.uyuni-project.org/"
ARG REFERENCE_PREFIX="registry.opensuse.org/uyuni"

# Build Service required labels
# labelprefix=org.opensuse.uyuni.proxy-ssh
LABEL org.opencontainers.image.title="${PRODUCT} proxy ssh container"
LABEL org.opencontainers.image.description="Image contains a ${PRODUCT} proxy component to serve and forward ssh access"
LABEL org.opencontainers.image.created="%BUILDTIME%"
LABEL org.opencontainers.image.vendor="${VENDOR}"
LABEL org.opencontainers.image.url="${URL}"
LABEL org.opencontainers.image.name=proxy-ssh-image
LABEL org.opencontainers.image.version=5.2.1
LABEL org.openbuildservice.disturl="%DISTURL%"
LABEL org.opensuse.reference="${REFERENCE_PREFIX}/proxy-ssh:${PRODUCT_VERSION}.%RELEASE%"
# endlabelprefix
LABEL org.uyuni.version="${PRODUCT_VERSION}"

# SSH port
EXPOSE 22

VOLUME "/etc/uyuni"

CMD uyuni-configure.py && /usr/sbin/sshd -D -e
