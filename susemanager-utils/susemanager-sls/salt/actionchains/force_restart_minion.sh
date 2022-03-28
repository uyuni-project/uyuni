#!/bin/bash
if [ "$(readlink /proc/1/exe)" = "/sbin/init" ]; then
   # SysV, use pid ctime as service start time
   T0=$(stat -c '%Z' /var/run/salt-minion.pid)
   RESTART_MINION="/usr/sbin/rcsalt-minion restart"
else
   # systemd
   TIME=$(systemctl show salt-minion --property=ActiveEnterTimestamp)
   TIME="${TIME//ActiveEnterTimestamp=/}"
   T0=$(date -d "$TIME" '+%s')
   RESTART_MINION="systemctl restart salt-minion"
fi

T1=$(date '+%s')
echo "salt-minion service uptime: $(( T1-T0 )) seconds"
if (( (T1-T0) > 5 )); then
   echo "Patch to update salt-minion was installed but service was not restarted. Forcing restart."
   $RESTART_MINION
fi
