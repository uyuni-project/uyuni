FROM rockylinux:8
COPY uyuni-tools-pool.repo /etc/yum.repos.d
RUN yum -y install openssh-server venv-salt-minion openssh-clients iproute hostname openscap-utils scap-security-guide-redhat udev dmidecode tar golang-github-prometheus-node_exporter prometheus-postgres_exporter
COPY test_repo_rpm_pool.repo /etc/yum.repos.d
COPY etc_pam.d_sshd /etc/pam.d/sshd
CMD ssh-keygen -A && /usr/sbin/sshd -De
