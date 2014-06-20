This directory contains all the files required to build the docker containers
used by our testing infrastructure.

## Container hierarchy

This is the hierarchy of our containers:

```
    +----------------+
    |  sle-<release> |
    +--------+-------+
             |
             |
             |
  +----------+---------+
  | suma-<branch>-base |
  +----------+---------+
             |
     +-------+--------------------+
     |                            |
     |                            |
+---------------------+ +---------+---------+
| suma-<branch>-pgsql | | suma-<branch>-ora |
+---------------------+ +-------------------+
```

We have one SUMa specific base container per each branch (1.7, 2.1, ..., head).
This container is based on "official" SLE container for the SUSE SLE release
user by the tested branch. These containers are external to the SUMa project,
they are built with kiwi and we do not have to maintain them.

All the SUMa-related containers are built using
[docker's build feature](http://docs.docker.io/en/latest/use/builder/).

From the `suma-<branch>-base` container we create two more containers:

  * `suma-<branch>-pgsql`
  * `suma-<branch>-ora`

These containers have all the packages required to test both the Java and the
Python codebase against either PostgreSQL or Oracle.

## Building

Right now the base containers are build manually, just enter their directory and
run:
```
docker build -t suma-<branch>-<base|pgsql|ora> .
```

The Oracle container requires some extra care:
  1) Run the following command: `docker run --privileged -t -i -v <dir containing git checkout>:/manager suma-<branch>-ora /bin/bash`
  2) From inside of the container run the following command: `/manager/susemanager-utils/testing/docker/<branch>/suma-<branch>-ora/setup-db-oracle.sh`
  3) Once the Oracle setup is done open a new terminal and run the following command: `docker commit <id of the container> suma-<branch>-ora`
     The id of the container is the hostname of the running container. Otherwise you can obtain it by doing: `docker ps`
These painful steps are going to disappear once docker's build system supports prileged containers.

## Custom registry

All the containers used by CI are stored inside of our personal docker registry
which runs on `suma-docker-registry.cloud.suse.de`.

### Push containers to our registry

First you need to tag the local images:
```
docker tag suma-<branch>-<base|pgsql|ora> suma-docker-registry.cloud.suse.de/suma-<branch>-<base|pgsql|ora>
```

Then you can push the local image:
```
docker push suma-docker-registry.cloud.suse.de/suma-<branch>-<base|pgsql|ora>
```
