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
# in /usr/share/rhn and HTTP dir.
# The assumption: CA in HTTP dir is an own created CA and we are a Server or Proxy
# This CA is copyied to the trust dir under the name LOCAL-RHN-ORG-TRUSTED-SSL-CERT
# If there is a CA in /usr/share/rhn it is expected to be the CA deployed by a
# registration. It is copied into the trust dir as RHN-ORG-TRUSTED-SSL-CERT.
# If the client is registered using salt, /usr/share/rhn might be empty the the state
# is copying the CA directly to the trust dir as RHN-ORG-TRUSTED-SSL-CERT.
# Finally the trust update is run.
#
# Optional argument: Certificate file name

CERT_DIR=/usr/share/rhn
CA_NAME="RHN-ORG-TRUSTED-SSL-CERT"
LOCAL_CA_NAME="LOCAL-RHN-ORG-TRUSTED-SSL-CERT"
if [ -n "$1" -a -f "$CERT_DIR/$1" ]; then
    CERT_FILE=$1
else
    CERT_FILE=$CA_NAME
fi
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

if [ -f $CA_HTTP_DIR/$CA_NAME ]; then
    test ! -f $CERT_DIR/$CERT_FILE || ! cmp -s $CERT_DIR/$CERT_FILE $CA_HTTP_DIR/$CA_NAME && {
        # this CA will be copied in the next step; we don't need it twice
        cp $CA_HTTP_DIR/$CA_NAME $TRUST_DIR/$LOCAL_CA_NAME
    }
else
    rm -f $TRUST_DIR/$LOCAL_CA_NAME
fi
if [ -f $CERT_DIR/$CERT_FILE ]; then
    cp $CERT_DIR/$CERT_FILE $TRUST_DIR/$CERT_FILE
fi

$UPDATE_TRUST_CMD
