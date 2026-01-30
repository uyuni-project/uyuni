# lzreposync

TODO: project description

## How to work in this project

1. Create a new virtual environment
```sh
$ python3.11 -m venv .venv
$ . .venv/bin/activate
```
2. Install `lzreposync` in *editable* mode
``` sh
$ pip install -e .
```
3. Install other required dependencies (required by spacewalk and other modules)
```sh
pip install rpm
pip install salt
```
4. Add a path configuration file (**Important!**)
```
echo "absolute/path/to/uyuni/python/" > .venv/lib64/python3.11/site-packages/uyuni_python_paths.pth
# This is a temporary solution that  will allow the lzreposync service to recognize/locate other modules like spacewalk, etc...
```
5. Add configuration environment variables
```sh
vim /etc/rhn/rhn.conf: # create directory/file if not exists

DB_BACKEND=postgresql
DB_USER=spacewalk
DB_PASSWORD=spacewalk
DB_NAME=susemanager
DB_HOST=127.0.0.1 # might not work with 'localhost'
DB_PORT=5432
PRODUCT_NAME=any
TRACEBACK_MAIL=any
DB_SSL_ENABLED=
DB_SSLROOTCERT=any
DEBUG=1
ENABLE_NVREA=1
MOUNT_POINT=/tmp
SYNC_SOURCE_PACKAGES=0

# Some values might not be the right ones
```
6. Try `lzreposync`
``` sh
$ lzreposync -u https://download.opensuse.org/update/leap/15.5/oss/ --type yum [--no-errata]
$ lzreposync --type deb --url 'https://ppa.launchpadcontent.net/longsleep/golang-backports/ubuntu?uyuni_suite=jammy&uyuni_component=main&uyuni_arch=amd64'
```

### How do I ...?

- add new a dependency? Add the *pypi* name to the `dependencies` list in the `[project]` section in `pyproject.toml`.

## Tests
We're using a special postgres db docker container that contains all the `susemanager` database schema built and ready.

To pull and start the database, you should:
```sh
cd /uyuni/java
sudo make -f Makefile.docker EXECUTOR=podman dockerrun_pg
# Wait a few seconds until the db is fully initialized
```

After installing with `pip install .` (or `pip install -e .`), `python3.11 -m pytest pytest tests/` runs all tests. Sometimes a `rehash` is required to ensure `.venv/bin/pytest` is used by your shell.

You can connect to the test database by:
```sh
psql -h localhost -d susemanager -U spacewalk # password: spacewalk
```

