#!/bin/bash
if [ "$(readlink /proc/1/exe)" = "/sbin/init" ]; then
   # SysV, use pid ctime as service start time
   SALT_MINION_NAME="salt-minion"
   SALT_MINION_PID="/var/run/salt-minion.pid"
   if [ -f /var/run/venv-salt-minion.pid ]; then
       SALT_MINION_NAME="venv-salt-minion"
       SALT_MINION_PID="/var/run/venv-salt-minion.pid"
   fi
   T0=$(stat -c '%Z' "$SALT_MINION_PID")
   RESTART_MINION="/usr/sbin/rc$SALT_MINION_NAME restart"
else
   # systemd
   SALT_MINION_NAME="salt-minion"
   if systemctl status venv-salt-minion > /dev/null 2>&1; then
       SALT_MINION_NAME="venv-salt-minion"
   fi
   TIME=$(systemctl show "$SALT_MINION_NAME" --property=ActiveEnterTimestamp)
   TIME="${TIME//ActiveEnterTimestamp=/}"
   T0=$(date -d "$TIME" '+%s')
   RESTART_MINION="systemctl restart $SALT_MINION_NAME"
fi

T1=$(date '+%s')
echo "salt-minion service uptime: $(( T1-T0 )) seconds"
if (( (T1-T0) > 5 )); then
   echo "Patch to update salt-minion was installed but service was not restarted. Forcing restart."
   $RESTART_MINION
fi
