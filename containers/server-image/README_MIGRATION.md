On host system:

ssh-keygen -t rsa
cat ~/.ssh/id_rsa.pub >> ~/.ssh/authorized_keys

set /etc/sysconfig/uyuni-server-systemd-services

systemctl start uyuni-server.service
