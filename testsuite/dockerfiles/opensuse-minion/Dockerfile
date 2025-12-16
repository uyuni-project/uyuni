FROM opensuse/tumbleweed
RUN zypper -n ar --no-gpgcheck https://download.opensuse.org/repositories/systemsmanagement:/Uyuni:/Stable/images/repo/Uyuni-Server-POOL-x86_64-Media1/ Uyuni-Server-POOL-x86_64 && \
    zypper -n ar --no-gpgcheck https://download.opensuse.org/repositories/systemsmanagement:/Uyuni:/Test-Packages:/Pool/rpm/ test_repo_rpm_pool && \
    zypper -n ar --no-gpgcheck https://download.opensuse.org/repositories/systemsmanagement:/Uyuni:/Stable:/Tumbleweed-Uyuni-Client-Tools/openSUSE_Tumbleweed/ tools_pool_repo && \
    zypper ref -f && \
    zypper -n install openssh-server openssh-clients libpwquality hostname iproute2 venv-salt-minion andromeda-dummy milkyway-dummy virgo-dummy openscap-utils openscap-content scap-security-guide gzip udev dmidecode tar \
      golang-github-prometheus-prometheus golang-github-prometheus-alertmanager prometheus-blackbox_exporter golang-github-prometheus-node_exporter golang-github-lusitaniae-apache_exporter prometheus-postgres_exporter golang-github-QubitProducts-exporter_exporter system-user-prometheus golang-github-prometheus-promu ansible mgr-push && \
    zypper clean -a

COPY etc_pam.d_sshd /etc/pam.d/sshd
RUN ssh-keygen -A
RUN echo "PermitRootLogin yes" > /etc/ssh/sshd_config.d/root.conf && \
    echo "ChallengeResponseAuthentication yes" >> /etc/ssh/sshd_config.d/root.conf

RUN groupadd -r --gid 10551 tomcat && \
    groupadd -r --gid 10552 www && \
    groupadd -r --gid 10553 wwwrun && \
    groupadd -r --gid 999 postgres
RUN useradd -r -s /usr/bin/bash -g postgres -d /var/lib/pgsql --uid 999 postgres

RUN set -euo pipefail; \
    zypper -n in --no-recommends systemd gzip container-suseconnect; \
    zypper -n clean; \
    rm -rf /var/log/*

# disabled as recommended by postgresql until we need to run systemd in container
# https://wiki.postgresql.org/wiki/Systemd
RUN mkdir -p /etc/systemd/logind.conf.d/ && \
    printf "[Login]\nRemoveIPC=no" > \
        /etc/systemd/logind.conf.d/disable-removeipc.conf

CMD ["/usr/lib/systemd/systemd"]
HEALTHCHECK --interval=5s --timeout=5s --retries=5 CMD ["/usr/bin/systemctl", "is-active", "multi-user.target"]
