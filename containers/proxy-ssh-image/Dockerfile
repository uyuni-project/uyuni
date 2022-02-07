#!BuildTag: proxy-ssh:latest proxy-ssh:%PKG_VERSION% proxy-ssh:%PKG_VERSION%.%RELEASE%

# HACK: Open Build Service currently only supports short names. Assume short name by default, allow to override to qualified name via ARG
ARG BASE_IMAGE_PREFIX=
FROM ${BASE_IMAGE_PREFIX}suse/sle15:15.3 AS fat

# Distro repos
RUN zypper addrepo http://download.opensuse.org/distribution/leap/15.3/repo/oss/ main
RUN zypper addrepo http://download.opensuse.org/update/leap/15.3/sle/ updates

# Main packages
RUN zypper --gpg-auto-import-keys --non-interactive install --auto-agree-with-licenses openssh-server openssh
# RUN ssh-keygen -A

RUN zypper --gpg-auto-import-keys --non-interactive install --auto-agree-with-licenses python3 python3-PyYAML

COPY mgr-proxy-ssh-force-cmd /usr/sbin/mgr-proxy-ssh-force-cmd

EXPOSE 22

VOLUME "/etc/uyuni"

COPY uyuni-configure.py /usr/bin/uyuni-configure.py
RUN chmod +x /usr/bin/uyuni-configure.py

CMD uyuni-configure.py && /usr/sbin/sshd -D -e
