These are the scripts to run the tests inside podman containers.
These scripts are run in order inside a github workflow and can also
be run manually from your local development workstation.

You can just call ./run to run them all or you can call them one
by one.

You can see the containers running with `podman ps` and connect
to any of them with `podman exec -ti container bash` .

For example you can connect to the controller one and run specific
tests with `cucumber path_to_test` .

Note you have to export the UYUNI_PROJECT and UYUNI_VERSION. This is
to download the right container.
