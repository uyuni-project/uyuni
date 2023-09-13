# Specify the license of the container build description (see also the LICENSE file)
# SPDX-License-Identifier: MIT
#!BuildTag: uyuni/init:latest

FROM opensuse/leap:15.5

# Define labels according to https://en.opensuse.org/Building_derived_containers
# labelprefix=org.opensuse.uyuni.init
LABEL org.opencontainers.image.title="Systemd image"
LABEL org.opencontainers.image.description="This container runs systemd"
LABEL org.opencontainers.image.version="4.4.0"
LABEL org.opensuse.reference="registry.opensuse.org/uyuni/init:4.4.0.%RELEASE%"
LABEL org.openbuildservice.disturl="%DISTURL%"
LABEL org.opencontainers.image.created="%BUILDTIME%"
LABEL org.opencontainers.image.vendor="Uyuni project"
LABEL org.opencontainers.image.url="https://www.uyuni-project.org/"
# endlabelprefix

# Fill the image with content and clean the cache(s)
RUN set -euo pipefail; zypper -n in --no-recommends systemd gzip; zypper -n clean; rm -rf /var/log/*
CMD ["/usr/lib/systemd/systemd"]

RUN mkdir -p /etc/systemd/system.conf.d/ && \
    printf "[Manager]\nLogColor=no" > \
        /etc/systemd/system.conf.d/01-nocolor.conf
RUN systemctl disable getty@tty1.service
HEALTHCHECK --interval=5s --timeout=5s --retries=5 CMD ["/usr/bin/systemctl", "is-active", "multi-user.target"]
