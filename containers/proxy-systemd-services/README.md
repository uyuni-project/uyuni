# Creating systemd services


## First things first

Create the pod; create containers and add them to the pod.


## Generate systemd services

```
mkdir systemd-services-generation
cd systemd-services-generation

podman generate systemd --files --name --new proxy-pod

# replace KillMode=none with TimeoutStopSec=60 as per https://github.com/containers/podman/pull/8889
sed -i 's/KillMode=none/TimeoutStopSec=60/' *-proxy-*.service

mv *-proxy-*.service /etc/systemd/system/.
```


## Start services
```
systemctl daemon-reload
systemctl start pod-proxy-pod.service
```


## NOTE

Remember to customize and parameterize values that are meant to be, because `podman generate systemd` will create services with the output value of each parameter based on the created/running instances.
