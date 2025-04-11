#!/bin/sh

set -e

KRB5CONFD="krb5.conf.d"

rm /etc/$KRB5CONFD/crypto-policies
ln -s /etc/crypto-policies/back-ends/krb5.config /etc/$KRB5CONFD/crypto-policies
mv "/etc/$KRB5CONFD" "/etc/rhn/$KRB5CONFD"
ln -s "/etc/rhn/$KRB5CONFD" "/etc/$KRB5CONFD"
