# uyuni containerization

based on code from https://gitlab.suse.de/mbologna/sumadocker

# Containers

This is a fat and ugly container that runs mostly everything. It is an initial step towards UYUNI containerization.

# Usage

## Build the container

% `sudo podman build  -t rmateus/uyuni .`

## Run the container

-with default volumes created:

% ```
sudo podman run --name uyuni-server -ti --hostname uyuni-server \
-p 4505:4505 -p 4506:4506 -p 443:443 -p 5432:5432 \
localhost/rmateus/uyuni
```

- with name volumes to future reuse:
% ```
sudo podman run --name uyuni-server -ti --hostname uyuni-server \
-p 4505:4505 -p 4506:4506 -p 443:443 -p 5432:5432 \
-v var_spacewalk:/var/spacewalk \
-v srv:/srv \
-v var_lib_pgsql:/var/lib/pgsql \
-v var_cache:/var/cache \
localhost/rmateus/uyuni
```


You will get a shell prompt.

Type `% ip a show eth0` and visit either `https://localhost` or `https://<container IP>` to access UYUNI.

## restart container

% `sudo podman start -a uyuni-server`
