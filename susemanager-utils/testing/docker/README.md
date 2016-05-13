This directory contains all the files required to build the docker containers
used by our testing infrastructure.

## Container hierarchy

This is the hierarchy of our containers:

```
                                       +------------------+
                                       |  sles11_spN_base |
                                       +--------+--------+
                                                |
                                     +----------+-------------+
                                     |                        |
                           +---------+--------+    +----------+--------+
                           | sles11_spN_pgsql |    | sles11_spN_oracle |
                           +---------+--------+    +----------+--------+
                                     |                        |
                                     |                        |
                +--------------------+---------+              +-----------------+-------------------------------+
                |                              |                                |                               |
+---------------+--------------+ +-------------+--------------+ +---------------+---------------+ +-------------+---------------+
| manager_python_testing_pgqsl | | manager_java_testing_pgsql | | manager_python_testing_oracle | | manager_java_testing_oracle |
+------------------------------+ +----------------------------+ +-------------------------------+ +-----------------------------+
```

We rely on different containers, all of them are built on top of a standard one
called `sle11_spN_base`, where N is the service pack number and may vary depending
on which release is supported. This container is built using kiwi while the other ones
are built using [docker's build feature](http://docs.docker.io/en/latest/use/builder/).

From the `sles11_spN_base` we create two more containers:

  * `sles11_spN_pgsql`: this is the base container plus the Postgresql server.
  * `sles11_spN_oracle`: this is the base container plus tools to access an external
  Oracle server.

These containers are used as base to create the systems for running the python
and java tests.

The `manager_python_testing_X` and `manager_java_testing_X` templates are
built starting from the same Dockerfile template. The differ in the parent
container used at build time.

## Building

The containers build process is implemented using [paver](http://paver.github.io/paver/),
a python implementation of GNU Make. Paver can be installed from the
[devel:languages:python](http://software.opensuse.org/package/python-Paver) repository.

Paver tasks are defined inside of files called `pavement.py`. A list of the
available tasks can be obtained by invoking `paver help` from the directory
containing the `pavement.py` file. More details about each task can be obtained
by invoking `paver help <task name>`.

The toplevel directory (`susemanager-utils/testing/docker`) contains a paver
file which allows to build the base containers, the db containers and the actual
testing containers.

### Base containers

To build the base containers just invoke:

`paver build_base_containers`

This will build the containers required to test all the branches covered by our
tests. Right now this will build a SLE11SP2 container (required to test the 1.7
branch) and a SLE11SP3 container (required to test the head branch). It's possible
to build containers only for certain branches using the '-b' cli option.

Once the containers are built the task will import them into the local docker
registry. This step can be skipped using the `-B` flag. This can be useful when
building the containers on a machine where docker is not installed.

A list of the supported branches can be obtained by invoking the following command:

`paver supported_branches`


### The db containers

The db containers can be built using the following command:

`paver build_db_containers`

This will build a container for each testing branch and for each db supported by
Manager. Right now it will produce the following containers:
  * sle11sp2 pgsql container.
  * sle11sp3 pgsql container.

A list of the supported dbs can be obtained by invoking the following command:

`paver supported_dbs`

### The testing containers

The containers used to run the test suite are created using the following command:

`paver build_testing_containers`

This builds a container for each testing target (java and python tests right now), 
testing branch and for each db supported by Manager. Right now this will produce
the following containers:
  * sle11sp2 pgsql python container.
  * sle11sp2 pgsql java container.
  * sle11sp3 pgsql python container.
  * sle11sp3 pgsql java container.

A list of the supported targets can be obtained by invoking the following command:

`paver supported_targets`

## Custom registry

All the containers used by CI are stored inside of our personal docker registry
which runs on `registry.mgr.suse.de`.

### Push containers to our registry

You can push the containers built on your machine to our private registry using
the dedicated paver tasks.

### Oracle tests

At the moment it is not possible to have an Oracle instance running inside a container
because of containers' limitations (access to /proc and AuFS are known to be problematic,
at least).

Thus, at the moment, Oracle containers access a virtual Oracle server (sumaoracletest.suse.de),
which is an Oracle for Testsuite instance available here:

http://download.suse.de/ibs/Devel:/Galaxy:/Manager:/Head:/Appliance/images/

In case it had to be replaced, just make sure relevant containers have passwordless SSH
access.

Note that the VM host, cokerunner.suse.de, uses the qemu "-snapshot" mode, which means
that all disk changes are restored upon VM reboot. This is used to restore the VM to a known
state weekly via a cron job on user root.

To manage the VM you can use the following scripts (on cokerunner.suse.de):

  * `/root/bin/run_oracle_server_used_by_docker`: to start the VM
  * `/root/bin/restart_oracle_server_used_by_docker`: to kill the running
    instance (if present) and start it again.

