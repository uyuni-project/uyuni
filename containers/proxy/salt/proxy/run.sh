#!/bin/sh

echo "Starting services..."

/usr/sbin/squid -FC
/usr/sbin/start_apache2 -k start
/usr/bin/salt-broker