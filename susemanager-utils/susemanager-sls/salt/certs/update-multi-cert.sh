CERT_DIR=/etc/ssl/certs
CERT_FILE=RHN-ORG-TRUSTED-SSL-CERT
TRUST_DIR=/etc/ssl/certs
rm -f $TRUST_DIR/${CERT_FILE}-*.pem
if [ -f $CERT_DIR/${CERT_FILE}.pem ]; then
    if [ $(grep -- "-----BEGIN CERTIFICATE-----" $CERT_DIR/${CERT_FILE}.pem | wc -l) -gt 1 ]; then
        csplit -b "%02d.pem" -f $TRUST_DIR/${CERT_FILE}- $CERT_DIR/${CERT_FILE}.pem '/-----BEGIN CERTIFICATE-----/' '{*}'
    fi
fi

