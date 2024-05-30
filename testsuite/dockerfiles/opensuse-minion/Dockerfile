FROM opensuse/leap:15.5
RUN zypper -n ar --no-gpgcheck https://download.opensuse.org/repositories/systemsmanagement:/Uyuni:/Stable/images/repo/Uyuni-Server-POOL-x86_64-Media1/ Uyuni-Server-POOL-x86_64 && \
    zypper -n ar --no-gpgcheck https://download.opensuse.org/repositories/systemsmanagement:/Uyuni:/Test-Packages:/Pool/rpm/systemsmanagement:Uyuni:Test-Packages:Pool.repo && \
    zypper ref -f && \
    zypper -n install openssh-server openssh-clients hostname iproute2 venv-salt-minion andromeda-dummy milkyway-dummy virgo-dummy openscap-utils openscap-content scap-security-guide gzip udev dmidecode tar prometheus && \
    zypper clean -a
RUN zypper -n ar --no-gpgcheck https://download.opensuse.org/repositories/systemsmanagement:/Uyuni:/Test-Packages:/Pool/rpm/ test_repo_rpm_pool
COPY etc_pam.d_sshd /etc/pam.d/sshd
CMD ssh-keygen -A && /usr/sbin/sshd -De
