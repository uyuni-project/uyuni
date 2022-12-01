FROM ubuntu:22.04
RUN echo "deb [trusted=yes] http://download.opensuse.org/repositories/systemsmanagement:/Uyuni:/Stable:/Ubuntu2204-Uyuni-Client-Tools/xUbuntu_22.04/ /" > /etc/apt/sources.list.d/uyuni-tools.list
RUN apt-get update && \
  apt-get -y install venv-salt-minion openssh-server && \
  apt-get clean
RUN mkdir /run/sshd
# run keygen in the container, not in the image
# this way, the key is not in the image
CMD ssh-keygen -A && /usr/sbin/sshd -De

