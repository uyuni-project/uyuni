#!BuildTag: proxy-ssh:latest proxy-ssh:%PKG_VERSION% proxy-ssh:%PKG_VERSION%.%RELEASE% proxy-ssh:beta

FROM registry.suse.com/suse/sle15:15.3

ARG PRODUCT_REPO

# Add distro and product repos
COPY add_repos.sh /usr/bin
RUN sh add_repos.sh ${PRODUCT_REPO}

# Build Service required labels
# labelprefix=org.opensuse.uyuni.proxy-ssh
LABEL org.opencontainers.image.title="Proxy ssh container"
LABEL org.opencontainers.image.description="Image contains a Proxy component to serve and forward ssh access"
LABEL org.opencontainers.image.created="%BUILDTIME%"
LABEL org.opencontainers.image.vendor="Uyuni project"
LABEL org.opencontainers.image.url="https://www.uyuni-project.org/"
LABEL org.opencontainers.image.version="%%PKG_VERSION%%"
LABEL org.openbuildservice.disturl="%DISTURL%"
LABEL org.opensuse.reference="registry.opensuse.org/proxy-ssh:%PKG_VERSION%.%RELEASE%"
# endlabelprefix

# Main packages
RUN zypper --gpg-auto-import-keys --non-interactive install --auto-agree-with-licenses openssh-server openssh
RUN zypper --gpg-auto-import-keys --non-interactive install --auto-agree-with-licenses python3 python3-PyYAML

# SSH port
EXPOSE 22

VOLUME "/etc/uyuni"

# Additional material
COPY mgr-proxy-ssh-force-cmd /usr/sbin/mgr-proxy-ssh-force-cmd

COPY uyuni-configure.py /usr/bin/uyuni-configure.py
RUN chmod +x /usr/bin/uyuni-configure.py

CMD uyuni-configure.py && /usr/sbin/sshd -D -e
