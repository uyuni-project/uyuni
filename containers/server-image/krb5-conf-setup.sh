#!/bin/sh

set -e

KRB5CONFD="krb5.conf.d"

mv "/etc/$KRB5CONFD" "/etc/rhn/$KRB5CONFD"
ln -s "/etc/rhn/$KRB5CONFD" "/etc/$KRB5CONFD"
