# Uyuni Server

[Uyuni](https://www.uyuni-project.org) is an open source systems management solution to deploy, configure, patch and monitor Linux systems.

Before installing, the following resources must exist in the target namespace:

- `kubernetes.io/basic-auth` secrets: `db-admin-credentials`, `db-credentials`, `reportdb-credentials` and `admin-credentials`
- TLS secrets: `db-cert` and `uyuni-cert`
- ConfigMaps with the root CA in the `ca.crt` key: `db-ca` and `uyuni-ca`

See the chart README for details.
