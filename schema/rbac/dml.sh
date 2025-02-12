#!/bin/bash
#
# Generate rbac data DML and execute on container server

set +x

python3 generate_dml.py data/endpoints/* > generated.sql

mgrctl cp ./generated.sql server:generated.sql
mgrctl exec -ti -- "spacewalk-sql -i < generated.sql"
rm generated.sql
