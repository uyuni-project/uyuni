FROM rockylinux:8
COPY uyuni-tools-pool.repo /etc/yum.repos.d
RUN yum -y install openssh-server venv-salt-minion
CMD ssh-keygen -A && /usr/sbin/sshd -De
