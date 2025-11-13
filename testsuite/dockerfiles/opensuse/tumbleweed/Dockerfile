FROM opensuse/tumbleweed
RUN zypper --non-interactive in avahi

RUN groupadd -r --gid 10551 tomcat && \
  groupadd -r --gid 10552 www && \
  groupadd -r --gid 10553 wwwrun && \
  groupadd -r --gid 999 postgres

RUN useradd -r -s /usr/bin/bash -g postgres -d /var/lib/pgsql --uid 999 postgres

# Fill the image with content and clean the cache(s)
RUN set -euo pipefail; zypper -n in --no-recommends systemd gzip container-suseconnect; zypper -n clean; rm -rf /var/log/*
CMD ["/usr/lib/systemd/systemd"]

RUN mkdir -p /etc/systemd/system.conf.d/ && \
    printf "[Manager]\nLogColor=no" > \
        /etc/systemd/system.conf.d/01-nocolor.conf

# disabled as recommended by postgresql until we need to run systemd in container
# https://wiki.postgresql.org/wiki/Systemd
RUN mkdir -p /etc/systemd/logind.conf.d/ && \
    printf "[Login]\nRemoveIPC=no" > \
        /etc/systemd/logind.conf.d/disable-removeipc.conf

RUN systemctl disable getty@tty1.service

HEALTHCHECK --interval=5s --timeout=5s --retries=5 CMD ["/usr/bin/systemctl", "is-active", "multi-user.target"]

