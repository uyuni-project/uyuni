This directory contains all the files required to build the docker containers
used by our testing infrastructure.

We rely on different containers, all of them are built on top of a standard one
called `sle11_sp2_base`. This container is built using kiwi and its configuration
files are located inside of the `base_containers` directory.

All the other containers are built using [docker's build feature](http://docs.docker.io/en/latest/use/builder/).

## Custom registry

All the containers used by CI are stored inside of our personal docker registry
which runs on `ix64smc161.qa.suse.de`.

### How to push containers to our registry

Right now the workflow is a bit complicated, hopefully docker registry will
provide something easier one day...

Just follow these steps:

  1. Import the container on machine running docker:
      `docker import - my_container < my_container_rootfs.tar`
  2. Insert the registry hostname inside of the container's tag:
      `docker tag my_container ix64smc161.qa.suse.de/my_container`
  3. Ensure the container name changed:
      `docker images`
  4. Push the container to our registry:
      `docker push ix64smc161.qa.suse.de/my_container`

Make sure you have a `~/.dockercfg` file before running step #4. If you do not
have it just create one containing bogus information (our registry does **not**
use authentication):

```
auth = foo
email = foo.bar.com
```

### Hot to pull containers from our registry

Also in this case there are a few glitches caused by docker's "freshness".

  1. Pull the image from the registry:
    `docker pull ix64smc161.qa.suse.de/my_container`
  2. Discover container's ID:
    `docker images`
  3. Tag the container:
    `docker tag <ID> <name>`

