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
