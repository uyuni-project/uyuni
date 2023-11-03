# Folders

  * `spacewalk` contains the backend code
  * `uyuni` contains the uyuni-commons code
  * `rhn` contains the rhnlib code
  * `test` contains all tests.

Keep the tests in `test/unit` runnable without a container.
New tests should be implemented using `pytest`.

*TODO*: the tests that are left in the code are in a broken state and should either be removed or converted to the new structure.


# Test dependencies:

  * python3-debian
  * pytest
  * python3-urlgrabber

# Running the tests

```
python3 -m pytest test/unit/
```

Running the integration tests requires a running postgresql database.
These can be run together with the unit tests as the CI does it within a container:

```
cd ../susemanager-utils/testing/automation
sh ./backend-unittest-pgsql.sh
```

To run the container manually:

```
docker run -t -i --rm=true -e "PYTHONPATH=/manager/python/:/manager/client/rhel/spacewalk-client-tools/src" \
       -v $UYUNI_ROOT:/manager registry.opensuse.org/systemsmanagement/uyuni/master/docker/containers/uyuni-master-pgsql \
       /bin/bash
```

Within the container shell, just run  `pytest /manager/python/test/` to run all tests.

# Linting

Uyuni uses `pylint` for enforcing code style, and `black` for automatic application of some of the style rules.

For pylint, Uyuni uses Google's [pylintrc](https://google.github.io/styleguide/pylintrc) configuration with the following modifications:

* Use 4 spaces instead of 2 for indentation.
* Set line length to 90 so that pylint is compatible with `black`.

To install `pylint` and `black` locally, use the `requirements-lint.txt` file:

```
# Create a venv
python3 -m venv .venv

# Source the new venv
. .venv/bin/activate

# Install pylint and black
pip install -r requirements-lint.txt
```

Then, you execute `black` and `pylint` to check linting:

```
# Auto-format a file
black /path/to/file.py

# Check pylint
pylint /path/to/file
```

To use the `pylintrc` file  from any directory, you can create a symbolic link to `~/.pylintrc`:

```
ln -s `pwd`/pylintrc ~/.pylintrc
```