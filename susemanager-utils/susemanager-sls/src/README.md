## Python Code Maintenance

Test are written with PyTest. This way:

1. Create your "test_foo.py" file.

2. Import with double-dot your package,
   so it will be included in the sys path, e.g.:

   from ..beacons import pkgset

3. Create a test function "def test_my_foo(..."

4. Rock-n-roll by simply calling "py.test".


Don't mind `.cache` and `__pycache__` directories,
they are ignored in an explicit `.gitignore`.

Have fun. :)

## Run Unit tests 

Use the following command to run unit test 
`make -f Makefile.python docker_pytest`

## Uyuni users state modules

### Pillar data structure global Admin User

Pillar data to configure suse manager admin user and password:
```
uyuni:
  xmlrpc:
    user: admin
    password: admin
```

### Available states 

Check methods in file `_states/uyuni_users.py` 

### Examples

#### Static state file

Example file:
`examples/uyuni_users_hardcode.sls`

#### Pillar Ldap data integration

**Organization information**

In order to manage user in organizations we need to have the user information (credential included) 
of the organization administrator.

One option is define organizations in a pillar data file. An example can be found at `examples/ldap/orgs.sls`.

**ldap Integration**

User and roles are obtained using the [pillar_ldap extension](https://docs.saltstack.com/en/latest/ref/pillar/all/salt.pillar.pillar_ldap.html). 
An example for the pillar ladp configuration can be found in `examples/ldap/pillar_ldap.yaml`

After changing the configuration file the salt master needs to be re-stared.

We need to extract fields to match the following parameters on uyuni users state:
- user name
- email
- first_name
- last_name
- roles


**State File**

The example state file takes leverage from the pillar data for organizations and users.
The SLS example file can be found at: `examples/ldap/uyuni_users_ldap.sls`
