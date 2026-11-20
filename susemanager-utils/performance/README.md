# SPDX-FileCopyrightText: 2026 SUSE LLC
#
# SPDX-License-Identifier: Apache-2.0

# Performance tests

This directory contains various performance tests.

### Locust

This tests are load tests using Locust.io

### copy-system.py

Creates fake systems by cloning an existing one directly in the database

### populate-fake-data.py

Fills the database with fake channels, packages, errata and Salt minions
subscribed to them, with outdated packages and needed errata. No package file
is created. Run it with `--help` for the sizing options, and with `--clean`
to remove the generated data. The database connection comes from the
`--host`, `--port`, `--user`, `--password` and `--db` flags, then from the
`PGHOST`, `PGPORT`, `PGUSER`, `PGPASSWORD` and `PGDATABASE` variables, and
finally from `/etc/rhn/rhn.conf`.
