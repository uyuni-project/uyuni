#!/bin/bash
HOST_TZ=$(timedatectl | awk '/Time zone:/{print $3}')
sed "s|^TZ=.*$|TZ=$HOST_TZ|" -i /etc/sysconfig/uyuni-server-systemd-services
