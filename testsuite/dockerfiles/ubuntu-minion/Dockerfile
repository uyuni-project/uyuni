FROM ubuntu:24.04
RUN echo "deb [trusted=yes] http://download.opensuse.org/repositories/systemsmanagement:/Uyuni:/Stable:/Ubuntu2404-Uyuni-Client-Tools/xUbuntu_24.04/ /" > /etc/apt/sources.list.d/uyuni-tools.list
RUN apt-get update && \
  apt-get -y install ca-certificates && \
  apt-get -y install venv-salt-minion openssh-server openssh-client hostname iproute2 iputils-ping openscap-utils openscap-scanner openscap-common ssg-debderived udev dmidecode tar \
    prometheus-node-exporter prometheus-apache-exporter prometheus-postgres-exporter prometheus-exporter-exporter prometheus-apache-exporter prometheus-node-exporter xz-utils && \
  apt-get clean
RUN echo "deb [trusted=yes] http://download.opensuse.org/repositories/systemsmanagement:/Uyuni:/Test-Packages:/Pool/deb/ /" > /etc/apt/sources.list.d/test_repo_deb_pool.list
RUN mkdir /run/sshd
COPY etc_pam.d_sshd /etc/pam.d/sshd
CMD ssh-keygen -A && /usr/sbin/sshd -De
