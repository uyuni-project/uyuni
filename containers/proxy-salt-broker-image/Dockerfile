#!BuildTag: uyuni/proxy-salt-broker:latest uyuni/proxy-salt-broker:%PKG_VERSION% uyuni/proxy-salt-broker:%PKG_VERSION%.%RELEASE% uyuni/proxy-salt-broker:beta

ARG BASE=registry.suse.com/bci/bci-base:15.3
FROM $BASE AS fat

ARG PRODUCT_REPO

# Add distro and product repos
COPY add_repos.sh /usr/bin
RUN sh add_repos.sh ${PRODUCT_REPO}

# Main packages
RUN zypper --gpg-auto-import-keys --non-interactive install --auto-agree-with-licenses spacewalk-proxy-salt

# Additional material
COPY uyuni-configure.py /usr/bin/uyuni-configure.py
RUN chmod +x /usr/bin/uyuni-configure.py

# Prepare for slimming down
COPY prepare_target.sh .
RUN sh prepare_target.sh

# Define slim image
ARG BASE=registry.suse.com/bci/bci-base:15.3
FROM $BASE AS slim

RUN zypper --gpg-auto-import-keys --non-interactive install --auto-agree-with-licenses python3 python3-PyYAML python3-pyzmq
COPY --from=fat /target /

# Build Service required labels
# labelprefix=org.opensuse.uyuni.proxy-salt-broker
LABEL org.opencontainers.image.title="Proxy salt-broker container"
LABEL org.opencontainers.image.description="Image contains a Proxy component to forward Salt connections"
LABEL org.opencontainers.image.created="%BUILDTIME%"
LABEL org.opencontainers.image.vendor="Uyuni project"
LABEL org.opencontainers.image.url="https://www.uyuni-project.org/"
LABEL org.opencontainers.image.version="%%PKG_VERSION%%"
LABEL org.openbuildservice.disturl="%DISTURL%"
LABEL org.opensuse.reference="registry.opensuse.org/uyuni/proxy-salt-broker:%PKG_VERSION%.%RELEASE%"
# endlabelprefix

# Salt
EXPOSE 4505/tcp
EXPOSE 4506/tcp

VOLUME "/etc/uyuni"

CMD uyuni-configure.py && salt-broker
