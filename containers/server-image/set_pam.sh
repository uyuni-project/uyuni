#!/bin/bash

pam-config -a --sss
pam-config -a --ldap

if command -v authselect ; then #requires nss-pam-ldapd and authselect
    authselect select sssd --force
    authselect apply-changes
elif command -v authconfig; then  #requires pam_ldap and authconfig
    authconfig --enablesssdauth --update
else
    echo "cannot use authselect or authconfig"
    exit 1
fi

