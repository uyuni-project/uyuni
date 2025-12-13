#!/bin/bash

####################################################################
### Use for testing only
####################################################################
DIR=demoCA
PASSWORD="secret"

# rsa, or any of "openssl ecparam -list_curves"
#PKEYALGO="secp384r1"
PKEYALGO="rsa"

ROOTCA="RootCA"
ORGCA="OrgCa"
TEAMCA="TeamCA"
SRVCRT="server"
SRVALTNAME="DNS:server.fqdn"
DBCRT="db"
DBALTNAME="DNS:db,DNS:reportdb,$SRVALTNAME"

export country="DE"
export state="STATE"
export city="CITY"
export org="ORG"
export orgunit="ORGUNIT"

mkdir -p $DIR/requests
mkdir -p $DIR/private
chmod 0700 $DIR/private
mkdir -p $DIR/certs
mkdir -p $DIR/newcerts
touch $DIR/index.txt

cat > $DIR/openssl.cnf <<OPENSSLCNF
# ca-openssl.cnf

[ ca ]
default_ca              = CA_default

[ CA_default ]
default_bits            = 2048
x509_extensions         = ca_x509_extensions
dir                     = ./$DIR
database                = \$dir/index.txt
serial                  = \$dir/serial
new_certs_dir           = \$dir/newcerts
default_md              = sha384
default_days            = 365

# how closely we follow policy
policy                  = policy_optional
copy_extensions         = copy

[ policy_optional ]
countryName             = optional
stateOrProvinceName     = optional
organizationName        = optional
organizationalUnitName  = optional
commonName              = optional
emailAddress            = optional

#---------------------------------------------------------------------------

[ req ]
default_bits            = 2048
distinguished_name      = req_distinguished_name
prompt                  = no

[ req_distinguished_name ]
C                       = \${ENV::country}
ST                      = \${ENV::state}
L                       = \${ENV::city}
O                       = \${ENV::org}
OU                      = \${ENV::orgunit}
CN                      = \${ENV::commonname}
#emailAddress            = ""


[ req_ca_x509_extensions ]
basicConstraints = CA:true
subjectKeyIdentifier = hash
keyUsage = digitalSignature, keyEncipherment, keyCertSign
extendedKeyUsage = serverAuth, clientAuth
# PKIX recommendations harmless if included in all certificates.
nsComment               = "SSL Generated Certificate"
authorityKeyIdentifier = keyid, issuer:always

[ req_server_csr_x509_extensions ]
basicConstraints = CA:false
subjectKeyIdentifier = hash
keyUsage = digitalSignature, keyEncipherment
extendedKeyUsage = serverAuth, clientAuth
nsCertType = server
# PKIX recommendations harmless if included in all certificates.
nsComment               = "SSL Generated Certificate"
subjectAltName          = \${ENV::subaltname}

[ req_server_x509_extensions ]
basicConstraints = CA:false
keyUsage = digitalSignature, keyEncipherment
extendedKeyUsage = serverAuth, clientAuth
nsCertType = server
# PKIX recommendations harmless if included in all certificates.
nsComment               = "SSL Generated Certificate"
subjectKeyIdentifier    = hash
authorityKeyIdentifier  = keyid, issuer:always
subjectAltName          = \${ENV::subaltname}

#===========================================================================
OPENSSLCNF

export commonname=$ROOTCA
export subaltname=""

if [ $PKEYALGO = "rsa" ]; then
  openssl genrsa -out $DIR/$ROOTCA.key -passout pass:$PASSWORD -aes256 2048
else
  openssl ecparam -genkey -name $PKEYALGO | openssl ec -aes256 -passout pass:$PASSWORD -out $DIR/$ROOTCA.key
fi

openssl req -config $DIR/openssl.cnf -extensions req_ca_x509_extensions -new -x509 -key $DIR/$ROOTCA.key -out $DIR/$ROOTCA.crt -days 1024 -passin pass:$PASSWORD

#-----------------------------------------------------------------
export commonname=$ORGCA
if [ $PKEYALGO = "rsa" ]; then
  openssl genrsa -out $DIR/private/$commonname.key -passout pass:$PASSWORD -aes256 2048
else
  openssl ecparam -genkey -name $PKEYALGO | openssl ec -aes256 -passout pass:$PASSWORD -out $DIR/private/$commonname.key
fi


openssl req -config $DIR/openssl.cnf -extensions req_ca_x509_extensions -new -key $DIR/private/$commonname.key -out $DIR/requests/$commonname.csr -passin pass:$PASSWORD

openssl ca -config $DIR/openssl.cnf -create_serial -extensions req_ca_x509_extensions -in $DIR/requests/$commonname.csr -keyfile $DIR/$ROOTCA.key \
        -cert $DIR/$ROOTCA.crt -passin pass:$PASSWORD -out $DIR/certs/$commonname.crt -days 500 -batch

#-----------------------------------------------------------------
export commonname=$TEAMCA
if [ $PKEYALGO = "rsa" ]; then
  openssl genrsa -out $DIR/private/$commonname.key -passout pass:$PASSWORD -aes256 2048
else
  openssl ecparam -genkey -name $PKEYALGO | openssl ec -aes256 -passout pass:$PASSWORD -out $DIR/private/$commonname.key
fi

openssl req -config $DIR/openssl.cnf -extensions req_ca_x509_extensions -new -key $DIR/private/$commonname.key -out $DIR/requests/$commonname.csr -passin pass:$PASSWORD

openssl ca -config $DIR/openssl.cnf -create_serial -extensions req_ca_x509_extensions -in $DIR/requests/$commonname.csr -keyfile $DIR/private/$ORGCA.key \
        -cert $DIR/certs/$ORGCA.crt -passin pass:$PASSWORD -out $DIR/certs/$commonname.crt -days 400 -batch

#-----------------------------------------------------------------
export commonname=$SRVCRT
export subaltname=$SRVALTNAME
if [ $PKEYALGO = "rsa" ]; then
  openssl genrsa -out $DIR/private/$commonname.key -passout pass:$PASSWORD -aes256 2048
else
  openssl ecparam -genkey -name $PKEYALGO | openssl ec -aes256 -passout pass:$PASSWORD -out $DIR/private/$commonname.key
fi

openssl req -config $DIR/openssl.cnf -extensions req_server_csr_x509_extensions -new -key $DIR/private/$commonname.key -out $DIR/requests/$commonname.csr -passin pass:$PASSWORD

openssl ca -config $DIR/openssl.cnf -create_serial -extensions req_server_x509_extensions -in $DIR/requests/$commonname.csr -keyfile $DIR/private/$TEAMCA.key \
        -cert $DIR/certs/$TEAMCA.crt -passin pass:$PASSWORD -out $DIR/certs/$commonname.crt -days 365 -batch

mkdir -p $DIR/package
openssl x509 -text -in $DIR/$ROOTCA.crt > $DIR/package/root-ca.crt
cat $DIR/certs/$ORGCA.crt $DIR/certs/$TEAMCA.crt > $DIR/package/intermediate-ca.crt
cp $DIR/certs/$SRVCRT.crt $DIR/package/server.crt
if [ $PKEYALGO = "rsa" ]; then
  openssl rsa -passin pass:$PASSWORD -in $DIR/private/$SRVCRT.key -out $DIR/package/server.key
else
  openssl ec -passin pass:$PASSWORD -in $DIR/private/$SRVCRT.key -out $DIR/package/server.key
fi

export commonname=$DBCRT
export subaltname=$DBALTNAME
if [ $PKEYALGO = "rsa" ]; then
  openssl genrsa -out $DIR/private/$commonname.key -passout pass:$PASSWORD -aes256 2048
else
  openssl ecparam -genkey -name $PKEYALGO | openssl ec -aes256 -passout pass:$PASSWORD -out $DIR/private/$commonname.key
fi

openssl req -config $DIR/openssl.cnf -extensions req_server_csr_x509_extensions -new -key $DIR/private/$commonname.key -out $DIR/requests/$commonname.csr -passin pass:$PASSWORD

openssl ca -config $DIR/openssl.cnf -create_serial -extensions req_server_x509_extensions -in $DIR/requests/$commonname.csr -keyfile $DIR/private/$TEAMCA.key \
        -cert $DIR/certs/$TEAMCA.crt -passin pass:$PASSWORD -out $DIR/certs/$commonname.crt -days 365 -batch

mkdir -p $DIR/package
openssl x509 -text -in $DIR/$ROOTCA.crt > $DIR/package/root-ca.crt
cat $DIR/certs/$ORGCA.crt $DIR/certs/$TEAMCA.crt > $DIR/package/intermediate-ca.crt
cp $DIR/certs/$DBCRT.crt $DIR/package/db.crt
if [ $PKEYALGO = "rsa" ]; then
  openssl rsa -passin pass:$PASSWORD -in $DIR/private/$DBCRT.key -out $DIR/package/db.key
else
  openssl ec -passin pass:$PASSWORD -in $DIR/private/$DBCRT.key -out $DIR/package/db.key
fi

echo "Test Certificates in $DIR/package/"
