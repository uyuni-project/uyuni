FROM registry.suse.com/bci/bci-base:15.4
RUN zypper -n ar --no-gpgcheck https://download.opensuse.org/repositories/systemsmanagement:/Uyuni:/Stable/images/repo/Uyuni-Server-POOL-x86_64-Media1/ Uyuni-Server-POOL-x86_64 && \
    zypper ref -f && \ 
    zypper -n install openssh-server venv-salt-minion && \
    zypper clean -a
CMD ssh-keygen -A && /usr/sbin/sshd -De
