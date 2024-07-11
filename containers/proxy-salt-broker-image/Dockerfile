# SPDX-License-Identifier: MIT
#!BuildTag: uyuni/proxy-salt-broker:latest

ARG BASE=registry.suse.com/bci/bci-base:15.5
FROM $BASE AS fat

ARG PRODUCT_REPO

# Add distro and product repos
COPY add_repos.sh /usr/bin
RUN sh add_repos.sh ${PRODUCT_REPO}

# Main packages
RUN zypper --gpg-auto-import-keys --non-interactive install --auto-agree-with-licenses spacewalk-proxy-salt && zypper clean --all

# Additional material
COPY uyuni-configure.py /usr/bin/uyuni-configure.py
RUN chmod +x /usr/bin/uyuni-configure.py

# Prepare for slimming down
COPY prepare_target.sh .
RUN sh prepare_target.sh

# Define slim image
ARG BASE=registry.suse.com/bci/bci-base:15.5
FROM $BASE AS slim

ARG PRODUCT=Uyuni
ARG VENDOR="Uyuni project"
ARG URL="https://www.uyuni-project.org/"
ARG REFERENCE_PREFIX="registry.opensuse.org/uyuni"

RUN zypper --gpg-auto-import-keys --non-interactive install --auto-agree-with-licenses python3 python3-PyYAML python3-pyzmq && zypper clean --all
COPY --from=fat /target /

# Build Service required labels
# labelprefix=org.opensuse.uyuni.proxy-salt-broker
LABEL org.opencontainers.image.title="${PROJECT} proxy salt-broker container"
LABEL org.opencontainers.image.description="Image contains a ${PRODUCT} proxy component to forward Salt connections"
LABEL org.opencontainers.image.created="%BUILDTIME%"
LABEL org.opencontainers.image.vendor="${VENDOR}"
LABEL org.opencontainers.image.url="${URL}"
LABEL org.opencontainers.image.name=proxy-salt-broker-image
LABEL org.opencontainers.image.version=5.1.0
LABEL org.openbuildservice.disturl="%DISTURL%"
LABEL org.opensuse.reference="${REFERENCE_PREFIX}/proxy-salt-broker:${PRODUCT_VERSION}.%RELEASE%"
# endlabelprefix
LABEL org.uyuni.version="${PRODUCT_VERSION}"

# Salt
EXPOSE 4505/tcp
EXPOSE 4506/tcp

VOLUME "/etc/uyuni"

CMD uyuni-configure.py && salt-broker
