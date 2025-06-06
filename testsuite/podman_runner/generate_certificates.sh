#!/bin/bash
    
CERT_O="test_org" 
CERT_OU="test_ou" 
CERT_CITY="test_city" 
CERT_STATE="test_state" 
CERT_COUNTRY="DE" 
CERT_EMAIL="a@b.com" 
CERT_CNAMES="server" 
CERT_PASS="spacewalk" 
HOSTNAME="server"

echo "Generating the self-signed SSL CA..."
mkdir -p /root/ssl-build
mkdir -p /ssl
rhn-ssl-tool --gen-ca --force --dir /root/ssl-build \
    --password $CERT_PASS \
    --set-country $CERT_COUNTRY --set-state $CERT_STATE --set-city $CERT_CITY \
    --set-org $CERT_O --set-org-unit $CERT_OU \
    --set-common-name $HOSTNAME --cert-expiration 3650
cp /root/ssl-build/RHN-ORG-TRUSTED-SSL-CERT /ssl/ca.crt

echo "Generate apache certificate..."
cert_args=""
for CERT_CNAME in $CERT_CNAMES; do
    cert_args="$cert_args --set-cname $CERT_CNAME"
done

rhn-ssl-tool --gen-server --cert-expiration 3650 \
    --dir /root/ssl-build --password $CERT_PASS \
    --set-country $CERT_COUNTRY --set-state $CERT_STATE --set-city $CERT_CITY \
    --set-org $CERT_O --set-org-unit $CERT_OU \
    --set-hostname $HOSTNAME --cert-expiration 3650 --set-email $CERT_EMAIL \
    $cert_args

NAME=${HOSTNAME%%.*}
cp /root/ssl-build/${NAME}/server.crt /ssl/server.crt
cp /root/ssl-build/${NAME}/server.key /ssl/server.key
# Copy the cert and key for the nginx server to redirect to the right port for the docker test to pass
cp /root/ssl-build/${NAME}/server.crt /tmp/server-nginx.crt
echo "${CERT_PASS}" | openssl rsa -in /ssl/server.key -out "/tmp/server-nginx.key" -passin pass:"${CERT_PASS}"

echo "Generating DB certificate..."
rhn-ssl-tool --gen-server --cert-expiration 3650 \
    --dir /root/ssl-build --password $CERT_PASS \
    --set-country $CERT_COUNTRY --set-state $CERT_STATE --set-city $CERT_CITY \
    --set-org $CERT_O --set-org-unit $CERT_OU \
    --set-hostname reportdb.mgr.internal --cert-expiration 3650 --set-email $CERT_EMAIL \
    $cert_args

cp /root/ssl-build/reportdb/server.crt /ssl/reportdb.crt
cp /root/ssl-build/reportdb/server.key /ssl/reportdb.key
