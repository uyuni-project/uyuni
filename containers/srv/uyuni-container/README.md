# uyuni containerization

based on code from https://gitlab.suse.de/mbologna/sumadocker

# Containers

This is a fat and ugly container that runs mostly everything. It is an initial step towards UYUNI containerization.

# Usage

## Build the container

`% sudo podman build -t uyuni .`

## Run the container

-with default volumes created:

```
sudo podman run --name hub-opensuse152 -ti --hostname hub-opensuse152 \
-p 4505:4505 -p 4506:4506 -p 443:443 -p 5432:5432 \
localhost/uyuni
```
You will get a shell prompt.

Type `% ip a show eth0` and visit either `https://localhost` or `https://<container IP>` to access UYUNI.

## restart container

`% sudo podman start -a uyuni-server`

## work around

Vm in libvirt created by sumaform:
  - change `/etc/hosts` and puth the name without `.tf.local` first, to be used as primary fqdn


sudo podman pod create -n uyuni -p 4505:4505 -p 4506:4506 -p 443:443 -p 5432:5432 --hostname uyuni  --name uyuni

sudo podman run --pod uyuni -ti --name uyuni-server localhost/uyuni
