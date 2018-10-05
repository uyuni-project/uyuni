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
          +-----------------------+----------------------+
          |                       |                      |
          |                       |                      |
+---------+-----------+ +---------+---------+ +----------+----------+
| suma-<branch>-pgsql | | suma-<branch>-ora | |suma-<branch>-nodejs |
+---------------------+ +-------------------+ +---------------------+
```

We have one SUMa specific base container per each branch (1.7, 2.1, ..., head).
This container is based on "official" SLE container for the SUSE SLE release
user by the tested branch. These containers are external to the SUMa project,
they are built with kiwi and we do not have to maintain them.

All the SUMa-related containers are built using
[docker's build feature](http://docs.docker.io/en/latest/use/builder/).

From the `suma-<branch>-base` container we create three more containers:

  * `suma-<branch>-pgsql`
  * `suma-<branch>-ora`
  * `suma-<branch>-nodejs`

These containers have all the packages required to test both the Java and the
Python codebase against either PostgreSQL or Oracle.

## Building

Right now the base containers are build manually, just enter their directory and
run:
```
docker build -t suma-<branch>-<base|pgsql|ora|nodejs> .
```

The Oracle container requires some extra care:
  1) Run the following command: `docker run --privileged -t -i -v <dir containing git checkout>:/manager suma-<branch>-ora /bin/bash`
  2) From inside of the container run the following command: `/manager/susemanager-utils/testing/docker/<branch>/suma-<branch>-ora/setup-db-oracle.sh`
     or `/manager/susemanager-utils/testing/docker/<branch>/suma-<branch>-ora/db-setup`
  3) Once the Oracle setup is done open a new terminal and run the following command: `docker commit <id of the container> suma-<branch>-ora`
     The id of the container is the hostname of the running container. Otherwise you can obtain it by doing: `docker ps`
These painful steps are going to disappear once docker's build system supports prileged containers.

## Tagging images

Docker images can be versioned. The special `latest` version is assumed when
no version is specified. This version is similar to `HEAD` in the context of
git.

Images can be tagged using the `docker tag` command. An existing tag can be
overwritten by using the `-f` switch.

A Docker image can have infinite tags. A tag is just some cheap metadata, so
no disk space is actually wasted.

Remember to provide the right version when dealing with Docker images:
  * `docker run foo:1.0.0`
  * `docker rmi foo:1.0.0`
  * `docker pull foo:1.0.0`
  * `docker push foo:1.0.0`

All these commands are interacting with version `1.0.0` of the `foo` image. When
`1.0.0` is not specified then `foo:latest` is being used.

Note well: it's highly recommended to use specific versions of an image when
writing a `Dockerfile`. Writing something like `FROM foo:1.0.0` makes you closer
to have reproducible builds.

### Tagging in the context of SUMA

At the end of the build process you are going to have a Docker image with
the `latest` tag. It's recommended to create also a new proper version pointing
to this image.

It is recommended to use [semantic versioning](http://semver.org/) when dealing
with tag versions.

For example, let's assume `suma-head-pgsql:1.0.0` already exists. Suppose you
updated the `Dockerfile` to add a missing package to this image.

After the `docker build` process is done the `suma-head-pgsql` image is going
to be the new Docker image including your package. Now you have to make sure
the image can be referenced by an explicit version:

`docker tag suma-head-pgsql:latest suma-head-pgsql:1.0.1`

Note well: `latest` could have been omitted; it has been written just to make
things more explicit.

## Custom registry

All the containers used by CI are stored inside of our personal docker registry
which runs on `registry.mgr.suse.de`.

### Push containers to our registry

First you need to tag the local images:
```
docker tag suma-<branch>-<base|pgsql|ora|nodejs> registry.mgr.suse.de/suma-<branch>-<base|pgsql|ora|nodejs>
docker tag suma-<branch>-<base|pgsql|ora|nodejs>:<version> registry.mgr.suse.de/suma-<branch>-<base|pgsql|ora|nodejs>:<version>
```

Then you can push the local image:
```
docker push registry.mgr.suse.de/suma-<branch>-<base|pgsql|ora|nodejs>
docker push registry.mgr.suse.de/suma-<branch>-<base|pgsql|ora|nodejs>:<version>
```
