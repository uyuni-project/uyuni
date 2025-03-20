#!/bin/bash
#
# Generate RBAC data DML and execute on container server
#
# $1: namespace to grant access recursively to the 'test'
# user (id=34) after all endpoints and namespaces are inserted
# If not provided, unlimited access is granted.

cat <(python3 generate_dml.py data/endpoints/*) \
    <(echo "call access.grant_access(34, '$1%');") \
    | mgrctl exec -i -- spacewalk-sql -i \
    && echo "RBAC data inserted."
