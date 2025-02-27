#!/bin/bash
#
# Generate RBAC data DML and execute on container server

cat <(python3 generate_dml.py data/endpoints/*) \
    <(echo "call access.grant_access(34, '%');") \
    | mgrctl exec -i -- spacewalk-sql -i \
    && echo "RBAC data inserted."
