FROM registry.suse.com/bci/bci-init:15.4
# from https://raw.githubusercontent.com/rjmateus/uyuni-container/main/uyuni/Dockerfile

RUN zypper ar -G http://download.opensuse.org/update/leap/15.4/backports/ backports_update_repo
RUN zypper ar -G http://download.opensuse.org/distribution/leap/15.4/repo/oss/ os_pool_repo
RUN zypper ar -G http://download.opensuse.org/update/leap/15.4/oss/ os_update_repo
RUN zypper ar -G http://download.opensuse.org/repositories/systemsmanagement:/Uyuni:/Stable/images/repo/Uyuni-Server-POOL-x86_64-Media1/ server_pool_repo
RUN zypper ar -G http://download.opensuse.org/update/leap/15.4/sle/ sle_update_repo
RUN zypper ar -G http://download.opensuse.org/repositories/systemsmanagement:/Uyuni:/Stable/images/repo/Testing-Overlay-POOL-x86_64-Media1/ testing_overlay_devel_repo

copy setup_env.sh /root/setup_env.sh

RUN zypper ref
RUN zypper -n in vi openssh-server
RUN zypper --non-interactive install --auto-agree-with-licenses --force-resolution patterns-uyuni_server
RUN zypper mr -d repo-backports-update repo-sle-update

# hack to correct the report db script to work on containers
RUN cp /usr/bin/uyuni-setup-reportdb /usr/bin/uyuni-setup-reportdb.original
RUN sed -i 's/sysctl kernel.shmmax/#sysctl kernel.shmmax/g' /usr/bin/uyuni-setup-reportdb
RUN sed -i 's/-a "$PG_SOCKET"//g' /usr/bin/uyuni-setup-reportdb

# hack to fix pam for podman
COPY etc_pam.d_sshd /etc/pam.d/sshd

EXPOSE 443/tcp
EXPOSE 80/tcp
EXPOSE 4505/tcp
EXPOSE 4506/tcp


CMD ["/usr/lib/systemd/systemd"]
HEALTHCHECK --interval=5s --timeout=5s --retries=5 CMD ["/usr/bin/systemctl", "is-active", "multi-user.target"]


