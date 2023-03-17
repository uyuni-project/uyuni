# Running the server-image on kubernetes

## Prerequisites

The following assumes you have a single-node rke2 or k3s cluster ready with enough resources for the Uyuni server.
It also assumes that `kubectl` and `helm` are installed on your machine and configured to connect to the cluster.

## Setting up the resources

### RKE2 specific setup

Copy the `rke2-ingress-nginx-config.yaml` file to `/var/lib/rancher/rke2/server/manifests/rke2-ingress-nginx-config.yaml` on your rke2 node.
Wait for the ingress controller to restart.
Run this command to watch it restart:

```
watch kubectl get -n kube-system pod -lapp.kubernetes.io/name=rke2-ingress-nginx
```

### K3s specific setup


Copy the `k3s-traefik-config.yaml` file to `/var/lib/rancher/k3s/server/manifests/` on your k3s node.
Wait for trafik to restart.
Run this commant to watch it restart:

```
watch kubectl get -n kube-system pod -lapp.kubernetes.io/name=traefik
```

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

Create a password-less SSH key and create a kubernetes secret with it:

```
ssh-keygen
kubectl create secret generic migration-ssh-key --from-file=id_rsa=$HOME/.ssh/id_rsa --from-file=id_rsa.pub=$HOME/.ssh/id_rsa.pub
```
Add the generated public key to the server to migrate authorized keys.

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


### CA certificates using `rhn-ssl-tool`

On the cluster node, prepare the volume with the CA password in the `/var/uyuni/ssl-build/password` file:

```
mkdir -p /var/uyuni/ssl-build
chmod 700 /var/uyuni
vim /var/uyuni/ssl-build/password
chmod 500 /var/uyuni/ssl-build/password
```

Edit the `rhn-ssl-tool.yaml` file to match your FQDN and subject.
Generate the CA certificate and server certificate and key using `rhn-ssl-tool` by running:

```
kubectl apply -f rhn-ssl-tool.yaml
```

**Note** that it pulls the big server container image and thus takes quite some time to complete.
Wait for the generated pod to be in `COMPLETED` state before continuing.

Create the TLS secret holding the server SSL certificates by running this on the cluster node:

```
kubectl create secret tls uyuni-cert --key /var/uyuni/ssl-build/<servername>/server.key --cert /var/uyuni/ssl-build/<servername>/server.crt
```

Create a `ConfigMap` with the CA certificate by running this on the cluster node:

```
kubectl create configmap uyuni-ca --from-file=ca.crt=/var/uyuni/ssl-build/RHN-ORG-TRUSTED-SSL-CERT
```

### CA certificates using Cert-Manager

Install cert-manager on the cluster.
The [default static install](https://cert-manager.io/docs/installation/#default-static-install) is enoughfor the testing use case:

```
kubectl apply -f https://github.com/cert-manager/cert-manager/releases/download/v1.11.0/cert-manager.yaml
```

`cert-manager` now needs to be configured to issue certificates.
The following instructions will document setting up a self signed CA and the corresponding issuers.
Check the [documentation](https://cert-manager.io/docs/configuration/acme/) on how to set up other issuers like Let's Encrypt.

Edit the `cert-manager-selfsigned-issuer.yaml` file to match the server FQDN and subject and then apply it:

```
kubectl apply -f cert-manager-selfsigned-issuer.yaml
```

For security reason, copy the CA certificate into a separate config map, to not mount the CA secret on the pod:

```
kubectl get secret uyuni-ca -o=jsonpath='{.data.ca\.crt}' | base64 -d >ca.crt
kubectl create configmap uyuni-ca --from-file=ca.crt
rm ca.crt
```

Run the following command to append the ingress annotation to use the new CA when applying the helm chart later:

```
cat >values.yaml << EOF
ingressSslAnnotations:
  cert-manager.io/issuer: uyuni-ca-issuer
EOF
```


### Deploy the pod and its resources


Change the hostname associated to the persistent volumes to match the hostname of your node:

```
sed 's/uyuni-dev/youhostname/' -i pvs.yaml
```

Define the persistent volumes by running `kubectl apply -f pvs.yaml`.
The volumes are folders on the cluster node and need to be manually created:

```
mkdir -p `kubectl get pv -o jsonpath='{.items[*].spec.local.path}'`
```

Run the following to add the helm chart configuration values but replace the `uyuni-dev.world-co.com` by your server's FQDN:

```
CAT >>values.yaml << EOF
repository: registry.opensuse.org/systemsmanagement/uyuni/master/servercontainer/containers/uyuni
storageClass: local-storage
exposeJavaDebug: true
uyuniMailFrom: notifications@uyuni-dev.world-co.com
fqdn: uyuni-dev.world-co.com
EOF
```

If deploying on `rke2`, add the `ingress: nginx` line to the `values.yaml` file.

You can also set more variables like `sccUser` or `sccPass`.
Check the [server-helm/values.yaml](https://github.com/uyuni-project/uyuni/blob/server-container/containers/server-helm/values.yaml) file for the complete list.

Install the helm chart from the source's `containers` folder:

```
helm install uyuni server-helm -f values
```

Note that the Helm chart installs a deployment with one replica.
The pod name is automatically generated by kubernetes and changes at every start.

The pod takes a while to start as it needs to initialize the mounts and run the setup.
Run `kubectl get pod -lapp=uyuni` and wait for it to be in `RUNNING` state.
Even after this, give it time to complete the setup during first boot.

You can monitor the progress of the setup with `kubectl exec $(kubectl get pod -lapp=uyuni -o jsonpath={.items[0].metadata.name}) -- tail -f /var/log/susemanager_setup.log`

## Using the pod

To getting a shell in the pod run `kubectl exec -ti $(kubectl get pod -lapp=uyuni -o jsonpath={.items[0].metadata.name}) -- sh`.
Note that the part after the `--` can be any command to run inside the server.

To copy files to the server, use the `kubectl cp <local_path> $(kubectl get pod -lapp=uyuni -o jsonpath={.items[0].metadata.name}):<remote_path>` command.
Run `kubectl cp --help` for more details on how to use it.

## Developping with the pod

###  Deploying code

To deploy java code on the pod change to the `java` directory and run:

```
ant -f manager-build.xml refresh-branding-jar deploy-restart-kube
```

In case you changed the pod namespace, pass the corresponding `-Ddeploy.namespace=<yourns>` parameter.

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

If you want to create from a fresh pod, run `helm uninstall uyuni`.

Then run this command on the cluster node to cleanup the volumes:

```
for v in `ls /var/uyuni/`; do
    rm -r /var/uyuni/$v; mkdir /var/uyuni/$v
done
```

To create the pod again, just run the Helm install again and wait.
