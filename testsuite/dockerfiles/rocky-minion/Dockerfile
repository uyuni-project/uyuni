FROM rockylinux:8
COPY uyuni-tools-pool.repo /etc/yum.repos.d
RUN yum -y install openssh-server venv-salt-minion openssh-clients iproute hostname openscap-utils scap-security-guide-redhat udev dmidecode
COPY test_repo_rpm_pool.repo /etc/yum.repos.d
CMD ssh-keygen -A && /usr/sbin/sshd -De
