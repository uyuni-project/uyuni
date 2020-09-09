# Uyuni configuration modules (`uyuni_config`) documentation

These execution and state modules allow to configure organizations, users, user permissions on channels and system groups on an Uyuni or SUSE Manager Server.

## General pillar data configuration

Virtually all functions in the modules leverage the XMLRPC API. It is thus necessary to provide an Uyuni/SUSE Manager administrator user name and password, with permissions on the entities to configure.

It is possible and recommended to configure those credentials in a pillar file with the following structure:
```
uyuni:
  xmlrpc:
    user: admin
    password: admin
```

## Detailed function documentation

Individual methods, parameters and return values are documented in `uyuni_config_execution_module_doc.txt` and `uyuni_config_state_module_doc.txt` in the same directory that contains this file.

## Examples

A few examples are provided:

- `examples/uyuni_config_hardcode.sls`: shows how to define an organization, a trust, a system group and a user with channel permissions. Note: all credentials are hardcoded directly in the file for simplicity's sake, but should at least be moved to pillars in a production environment
- `examples/ldap/uyuni_users_ldap.sls`: shows how to define multiple users based on data coming from an LDAP server via the LDAP pillar module. This allows to implement syncing LDAP users to Uyuni/SUSE Manager

### LDAP example specifics

Configuration notes:
- see  "General pillar data configuration" above for general credential configuration in pillars
- one more pillar needs to be defined in which organization administrator credentials are specified for each organization the state is going to create users in. An example with one organization can be found in `examples/ldap/pillar_orgs.yaml`
- in order to retrieve data from an LDAP server, the [pillar_ldap module](https://docs.saltstack.com/en/latest/ref/pillar/all/salt.pillar.pillar_ldap.html) is used, and needs its own configuration pillar. An example can be found in `examples/ldap/pillar_ldap.yaml`

In this particular example, the following LDAP fields are extracted in order to match corresponding Uyuni/SUSE Manager parameters:
- user name
- email
- first_name
- last_name
- roles
