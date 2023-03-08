# Running the server-image on kubernetes

## Prerequisites

The following assumes you have a single-node rke2 or k3s cluster ready with enough resources for the Uyuni server.
It also assumes that `kubectl` is installed on your machine and configured to connect to the cluster.

** HACK ** For now I used the SSL certificates and CA generated in one of my installation attempts.
I will assume you already have SSL certificates matching the FQDN of the cluster node.
Instructions or tools on how to generate those will come later.

## Setting up the resources

### RKE2 specific setup

Copy the `rke2-ingress-nginx-config.yaml` file to `/var/lib/rancher/rke2/server/manifests/rke2-ingress-nginx-config.yaml` on your rke2 node.
Wait for the ingress controller to restart.
Run this command to watch it restart:

```
watch kubectl get -n kube-system pod -lapp.kubernetes.io/name=rke2-ingress-nginx
```

Set the shell variable `INGRESS=nginx` to be used in the next steps.

### K3s specific setup


Copy the `k3s-traefik-config.yaml` file to `/var/lib/rancher/k3s/server/manifests/` on your k3s node.
Wait for trafik to restart.
Run this commant to watch it restart:

```
watch kubectl get -n kube-system pod -lapp.kubernetes.io/name=traefik
```

Set the shell variable `INGRESS=traefik` to be used in the next steps.

***Offline installation:*** with k3s it is possible to preload the container images and avoid it to be fetched from a registry.
For this, on a machine with internet access, pull the image using `podman`, `docker` or `skopeo` and save it as a `tar` archive.
For example:

```
podman pull registry.opensuse.org/systemsmanagement/uyuni/master/servercontainer/containers/uyuni/server:latest
podman save --output server-image.tar registry.opensuse.org/systemsmanagement/uyuni/master/servercontainer/containers/uyuni/server:latest
```

or

```
skopeo copy docker://registry.opensuse.org/systemsmanagement/uyuni/master/servercontainer/containers/uyuni/server:latest docker-archive:server-image.tar:registry.opensuse.org/systemsmanagement/uyuni/master/servercontainer/containers/uyuni/server:latest
```

Transfer the resulting `server-image.tar` to the k3s node and load it using the following command:

```
k3s ctr images import server-image.tar
```

In order to tell k3s to not pull the image, add `imagePullPolicy: Never` to all `initContainer`s and `container` in the `server.yaml` file:

```
sed 's/^\( \+\)image:\(.*\)$/\1image: \2\n\1imagePullPolicy: Never/' -i server.yaml
```

### Migrating from a regular server

Stop the source services:

```
spacewalk-service stop
systemctl stop postgresql
```

Run the migration job:

```
kubectl apply -f migration-job.yaml
```

To follow the progression of the process, check the generated container log:

```
kubectl logs (kubectl get pod -ljob-name=uyuni-migration -o custom-columns=NAME:.metadata.name --no-hea
ders)
```

Once done, both the job and its pod will remain until the user deletes them to allow checking logs.

Proceed with the next steps.

***Hostname***: this procedure doesn't handle any hostname change.
Certificates migration also needs to be documented, but that can be guessed for now with the instructions to setup a server from scratch.

### Deploy the pod and its resources

Create the TLS secret holding the server SSL certificates:

```
kubectl create secret tls uyuni-cert --key <pathto>/server.key --cert <pathto>/server.crt
```

Create a `ConfigMap` with the CA certificate:

```
kubectl create configmap uyuni-ca --from-file=ca.crt=<pathto>/RHN-ORG-TRUSTED-SSL-CERT
```

Define the persistent volumes by running `kubectl apply -f pvs.yaml`.
The volumes are folders on the cluster node and need to be manually created:

```
mkdir -p `kubectl get pv -o jsonpath='{.items[*].spec.local.path}'`
```

In my setup, the cluster node is named `uyuni-dev` and its FQDN is `uyuni-dev.world-co.com`.
You will need to replace those values in the yaml files.

Once done, run the following commands:

```
for YAML in pvcs service uyuni-config server $INGRESS-uyuni-ingress; do
    kubectl apply -f $YAML.yaml
done
```
The pod takes a while to start as it needs to initialize the mounts and run the setup.
Run `kubectl get pod uyuni` and wait for it to be in `RUNNING` state.
Even after this, give it time to complete the setup during first boot.

You can monitor the progress of the setup with `kubectl exec uyuni -- tail -f /var/log/susemanager_setup.log`

## Using the pod

To getting a shell in the pod run `kubectl exec -ti uyuni -- sh`.
Note that the part after the `--` can be any command to run inside the server.

To copy files to the server, use the `kubectl cp <local_path> uyuni:<remote_path>` command.
Run `kubectl cp --help` for more details on how to use it.

## Developping with the pod

###  Deploying code

To deploy java code on the pod change to the `java` directory and run:

```
ant -f manager-build.xml refresh-branding-jar deploy-restart-kube
```

In case you changed the pod name and namespace while deploying it, pass the corresponding `-Ddeploy.namespace=<yourns>` and `-Ddeploy.pod=<yourpod>` parameters.

**Note** To deploy TSX or Salt code, use the `deploy-static-resources-kube` and `deploy-salt-files-kube` tasks of the ant file.

### Attaching a java debugger

First enable the JDWP options in both tomcat and taskomatic using the following command:

```
ant -f manager-build.xml enable-java-debug-kube
```

Then restart tomcat and taskomatic using ant too:

```
ant -f manager-build.xml restart-tomcat-kube restart-taskomatic-kube
```

The debugger can now be attached to the usual ports (8000 for tomcat and 8001 for taskomatic) on the host FQDN.

## Throwing everything away

If you want to create from a fresh pod, run `kubectl delete pod uyuni`.

Then run this command on the cluster node to cleanup the volumes:

```
for v in `ls /var/uyuni/`; do
    rm -r /var/uyuni/$v; mkdir /var/uyuni/$v
done
```

To create the pod again, just run `kubectl apply -f server.yaml` and wait.
