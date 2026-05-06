FROM rockylinux/rockylinux:8@sha256:e8a49c5403b687db05d4d67333fa45808fbe74f36e683cec7abb1f7d0f2338c6
COPY uyuni-tools-pool.repo /etc/yum.repos.d
RUN yum -y install dnf-plugins-core openssh-server venv-salt-minion openssh-clients iproute iputils hostname openscap-utils scap-security-guide-redhat udev dmidecode tar \
      golang-github-prometheus-node_exporter golang-github-lusitaniae-apache_exporter prometheus-postgres_exporter
COPY test_repo_rpm_pool.repo /etc/yum.repos.d
COPY etc_pam.d_sshd /etc/pam.d/sshd
CMD ssh-keygen -A && /usr/sbin/sshd -De
