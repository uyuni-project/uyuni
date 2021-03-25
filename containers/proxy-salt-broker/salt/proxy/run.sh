#!/bin/sh

echo "Starting services..."

/usr/sbin/start_apache2 -k start
/usr/bin/salt-minion
