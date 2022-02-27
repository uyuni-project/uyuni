# Copyright (c) 2008--2016 Red Hat, Inc.
#
# This software is licensed to you under the GNU General Public License,
# version 2 (GPLv2). There is NO WARRANTY for this software, express or
# implied, including the implied warranties of MERCHANTABILITY or FITNESS
# FOR A PARTICULAR PURPOSE. You should have received a copy of GPLv2
# along with this software; if not, see
# http://www.gnu.org/licenses/old-licenses/gpl-2.0.txt.
#
# Red Hat trademarks are not licensed under GPLv2. No permission is
# granted to use or replicate Red Hat trademarks that are incorporated
# in this software or its documentation.
#
# Scripts that adds/removes RHN-ORG-TRUSTED-SSL-CERT into/from system-wide
# trusted certificates.
# The script checks if RHN-ORG-TRUSTED-SSL-CERT is present
# in /usr/share/rhn and HTTP dir. It checks, if they are the same.
# If not both are copied, otherwise only the existing are copied. If it's not found
# then it's ensured files do not exists. Finally the trust update
# is run.
#
# Optional argument: Certificate file name

CERT_DIR=/usr/share/rhn
CERT_FILE=${1:-RHN-ORG-TRUSTED-SSL-CERT}
CA_HTTP_DIR=/var/www/html/pub/
TRUST_DIR=/etc/pki/ca-trust/source/anchors
UPDATE_TRUST_CMD="/usr/bin/update-ca-trust extract"
if [ -d /etc/pki/ca-trust/source/anchors -a -x /usr/bin/update-ca-trust ]; then
    TRUST_DIR=/etc/pki/ca-trust/source/anchors
elif [ -d /etc/pki/trust/anchors/ -a -x /usr/sbin/update-ca-certificates ]; then
    # SLE 12+
    TRUST_DIR=/etc/pki/trust/anchors
    UPDATE_TRUST_CMD="/usr/sbin/update-ca-certificates"
    CA_HTTP_DIR=/srv/www/htdocs/pub/
elif [ -d /etc/ssl/certs -a -x /usr/bin/c_rehash ]; then
    # SLE 11
    TRUST_DIR=/etc/ssl/certs
    UPDATE_TRUST_CMD="/usr/bin/c_rehash"
    rm -f $TRUST_DIR/RHN-ORG-TRUSTED-SSL-CERT.pem
    rm -f $TRUST_DIR/RHN-ORG-TRUSTED-SSL-CERT-*.pem
    if [ -f $CERT_DIR/$CERT_FILE ]; then
        ln -sf $CERT_DIR/$CERT_FILE $TRUST_DIR/RHN-ORG-TRUSTED-SSL-CERT.pem
        if [ $(grep -- "-----BEGIN CERTIFICATE-----" $CERT_DIR/$CERT_FILE | wc -l) -gt 1 ]; then
            csplit -b "%02d.pem" -f $TRUST_DIR/RHN-ORG-TRUSTED-SSL-CERT- $CERT_DIR/$CERT_FILE '/-----BEGIN CERTIFICATE-----/' '{*}'
        fi
    fi
    $UPDATE_TRUST_CMD >/dev/null
    exit 0
fi

# Not on EL5
if [ ! -d $TRUST_DIR ]; then
    exit 0
fi

# cleanup
rm -f $TRUST_DIR/$CERT_FILE
rm -f $TRUST_DIR/OWN-$CERT_FILE

if [ -f $CERT_DIR/$CERT_FILE -a -f $CA_HTTP_DIR/$CERT_FILE ]; then
    # we are registered and have an own CA
    # compare the CAs. In case they are not the the same link both
    if cmp $CERT_DIR/$CERT_FILE $CA_HTTP_DIR/$CERT_FILE; then
        cp $CA_HTTP_DIR/$CERT_FILE $TRUST_DIR/$CERT_FILE
    else
        cp $CA_HTTP_DIR/$CERT_FILE $TRUST_DIR/OWN-$CERT_FILE
        cp $CERT_DIR/$CERT_FILE $TRUST_DIR/$CERT_FILE
    fi
elif [ -f $CA_HTTP_DIR/$CERT_FILE ]; then
    cp $CA_HTTP_DIR/$CERT_FILE $TRUST_DIR/$CERT_FILE
elif [ -f $CERT_DIR/$CERT_FILE ]; then
    cp $CERT_DIR/$CERT_FILE $TRUST_DIR/$CERT_FILE
fi

$UPDATE_TRUST_CMD
