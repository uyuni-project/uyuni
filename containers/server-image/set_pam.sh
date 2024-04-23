#!/bin/bash

pam-config -a --sss
pam-config -a --ldap

if command -v authselect ; then
    authselect select sssd --force
    authselect apply-changes
else
    authconfig --enablesssdauth --update
fi

