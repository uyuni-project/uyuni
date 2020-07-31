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

### Pillar data structure

Pillar data to configure suse manager admin user and password:
```
uyuni:
  xmlrpc:
    user: admin
    password: admin
```

### Available states

Check methods in file `_states/uyuni_users.py` 

### Example files

* Example static hardcode data `examples/uyuni_users_hardcode.sls`
