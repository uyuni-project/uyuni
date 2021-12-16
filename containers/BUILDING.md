# Building instructions for developers

## Running a local registry (in sumaform)

Add to `main.tf` and then `terraform apply`:

```hcl
module "registry" {
  source = "./modules/registry"
  base_configuration = module.base.configuration
  name = "registry"
}
```

More information at https://github.com/uyuni-project/sumaform/blob/master/README_ADVANCED.md.


## Running a local registry (as a container)

```
mkdir registry_storage
podman run --publish 5000:5000 -v `pwd`/registry_storage:/var/lib/registry docker.io/library/registry:2
```

Registry will be available on port 5000. If `wget <hostname_or_IP>:5000/` does not work, check that port is open in your firewall.

In case you get into this error while pulling:
```
Error processing tar file(exit status 1): there might not be enough IDs available in the namespace (requested 0:42 for /etc/shadow): lchown /etc/shadow: invalid argument
```

Use the following commands to fix the problem:
```
sudo touch /etc/sub{u,g}id
sudo usermod --add-subuids 10000-75535 $(whoami)
sudo usermod --add-subgids 10000-75535 $(whoami)
rm /run/user/$(id -u)/libpod/pause.pid
```


## Building all images and pushing them to a registry

```sh
sh build-proxy.sh
```

In case you get into this other error while pulling:
```
Error: error creating build container: The following failures happened while trying to pull image specified by "suse/sle15:15.3" based on search registries in /etc/containers/registries.conf:
* "localhost/suse/sle15:15.3": Error initializing source docker://localhost/suse/sle15:15.3: error pinging docker registry localhost: Get https://localhost/v2/: dial tcp [::1]:443: connect: connection refused
* "registry.opensuse.org/suse/sle15:15.3": Error initializing source docker://registry.opensuse.org/suse/sle15:15.3: Error reading manifest 15.3 in registry.opensuse.org/suse/sle15: name unknown
* "docker.io/suse/sle15:15.3": Error initializing source docker://suse/sle15:15.3: Error reading manifest 15.3 in docker.io/suse/sle15: errors:
denied: requested access to the resource is denied
unauthorized: authentication required (edited)
```

you are probably using an older `podman` version (2.1.1 for instance). Make sure you are on a newer version (3.4.4 for instance), remove all images and restart the build.

On Leap 15.3:
```
sudo zypper ar https://download.opensuse.org/repositories/Virtualization:/containers/openSUSE_Leap_15.3/ virtualization_container
sudo zypper install podman-3.4.4-lp153.2.2.x86_64
podman rmi --all --force
sh build-proxy.sh
```

## Running

Copy `run-proxy.sh` and execute it.

To kill all containers use `podman pod kill uyuni-proxy`.
