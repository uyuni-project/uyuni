#!/usr/bin/bash
: "${UYUNI_HOSTNAME:=}"
postconf -e "myhostname=${UYUNI_HOSTNAME}"
