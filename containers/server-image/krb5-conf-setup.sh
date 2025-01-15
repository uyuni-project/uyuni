#!/bin/sh

set -e

KRB5CONFD="krb5.conf.d"

rmdir "/etc/$KRB5CONFD"
mkdir "/etc/rhn/$KRB5CONFD"
ln -s "/etc/rhn/$KRB5CONFD" "/etc/$KRB5CONFD"
