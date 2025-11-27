# SPDX-License-Identifier: MIT
#!BuildTag: uyuni/proxy-salt-broker:latest

ARG BASE=registry.suse.com/bci/bci-base:15.7
FROM $BASE AS fat

# Main packages
RUN zypper --gpg-auto-import-keys --non-interactive install --auto-agree-with-licenses \
        spacewalk-proxy-salt \
        python3 \
        python3-PyYAML \
        python3-pyzmq && \
    zypper --non-interactive clean --all && \
    rpm -e zypper && \
    rm -rf /var/log/{alternatives.log,lastlog,tallylog,suseconnect.log,zypper.log,zypp/history,YaST2}

# LABELs
ARG PRODUCT=Uyuni
ARG VENDOR="Uyuni project"
ARG URL="https://www.uyuni-project.org/"
ARG REFERENCE_PREFIX="registry.opensuse.org/uyuni"

# Build Service required labels
# labelprefix=org.opensuse.uyuni.proxy-salt-broker
LABEL org.opencontainers.image.title="${PROJECT} proxy salt-broker container"
LABEL org.opencontainers.image.description="Image contains a ${PRODUCT} proxy component to forward Salt connections"
LABEL org.opencontainers.image.created="%BUILDTIME%"
LABEL org.opencontainers.image.vendor="${VENDOR}"
LABEL org.opencontainers.image.url="${URL}"
LABEL org.opencontainers.image.name=proxy-salt-broker-image
LABEL org.opencontainers.image.version=5.2.2
LABEL org.openbuildservice.disturl="%DISTURL%"
LABEL org.opensuse.reference="${REFERENCE_PREFIX}/proxy-salt-broker:${PRODUCT_VERSION}.%RELEASE%"
# endlabelprefix
LABEL org.uyuni.version="${PRODUCT_VERSION}"

# Salt
EXPOSE 4505/tcp
EXPOSE 4506/tcp

VOLUME "/etc/uyuni"

CMD salt-broker
