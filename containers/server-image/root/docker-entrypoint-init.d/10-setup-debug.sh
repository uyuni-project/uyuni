#!/usr/bin/bash
: "${DEBUG_JAVA:=false}"

if [ "${DEBUG_JAVA}" = "true" ]; then
    echo "Setting up Java debug options"
    # Note: $JAVA_OPTS inside single quotes is NOT expanded here. 
    # It assumes the target file is a shell script that will source this line later.
    if ! grep -q 'Xrunjdwp' /etc/tomcat/conf.d/remote_debug.conf; then
        echo 'JAVA_OPTS=" $JAVA_OPTS -Xdebug -Xrunjdwp:transport=dt_socket,address=*:8003,server=y,suspend=n" ' >> /etc/tomcat/conf.d/remote_debug.conf
    fi
    if ! grep -q 'Xrunjdwp' /etc/rhn/taskomatic.conf; then
        echo 'JAVA_OPTS=" $JAVA_OPTS -Xdebug -Xrunjdwp:transport=dt_socket,address=*:8001,server=y,suspend=n" ' >> /etc/rhn/taskomatic.conf
    fi
    if ! grep -q 'Xrunjdwp' /usr/share/rhn/config-defaults/rhn_search_daemon.conf; then
        echo 'JAVA_OPTS=" $JAVA_OPTS -Xdebug -Xrunjdwp:transport=dt_socket,address=*:8002,server=y,suspend=n" ' >> /usr/share/rhn/config-defaults/rhn_search_daemon.conf
    fi
else
    echo "Removing Java debug options"
    sed -i '/Xrunjdwp/d' /etc/tomcat/conf.d/remote_debug.conf
    sed -i '/Xrunjdwp/d' /etc/rhn/taskomatic.conf
    sed -i '/Xrunjdwp/d' /usr/share/rhn/config-defaults/rhn_search_daemon.conf
  fi
