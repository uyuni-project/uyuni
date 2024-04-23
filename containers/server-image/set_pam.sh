#!/bin/bash

pam-config -a --sss
pam-config -a --ldap
authselect select sssd --force
authselect apply-changes
