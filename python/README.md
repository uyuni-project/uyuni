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
