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

Uyuni uses [`pylint`](https://pylint.pycqa.org/en/latest/index.html) and [`black`](https://black.readthedocs.io/en/stable/) for enforcing and automatic application code linting and formatting.

For pylint, Uyuni uses Google's [pylintrc](https://google.github.io/styleguide/pylintrc) configuration with the following modifications:

* Use 4 spaces instead of 2 for indentation.
* Line length rule is disabled, because Black enforces line length of `88` characters per line (where possible).

Use the provided script to run `pylint` and `black` over a set of files:

```
linting/lint.sh python/spacewalk/satellite_tools/xmlWireSource.py python/spacewalk/satellite_tools/download.py
```

You can also pass the `-a` flag to lint modified (but not committed) files:

```
linting/lint.sh -a
```

Alternatively, you can use the provided container to run `pylint` and `black` manually:

```
podman run --rm -it -v $UYUNI_ROOT:/mgr registry.opensuse.org/home/mczernek/containers/opensuse_factory_containerfile/uyuni-lint:latest bash
black -t py36 path/to/file.py
pylint --rcfile=/root/.pylintrc /path/to/file.py
```

