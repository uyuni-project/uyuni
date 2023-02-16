# Prerequisites

The following assumes you have either a single-node RKE2 or K3s cluster ready or a server with Podman installed and enough resources for the Uyuni server.
When installing on a Kubernetes cluster, it also assumes that `kubectl` and `helm` are installed on the server and configured to connect to the cluster.

# Preparing the installation

## Podman specific setup

There is nothing to prepare for a Podman installation.

## RKE2 specific setup

Copy the `rke2-ingress-nginx-config.yaml` file to `/var/lib/rancher/rke2/server/manifests/rke2-ingress-nginx-config.yaml` on your RKE2 node.
Wait for the ingress controller to restart.
Run this command to watch it restart:

```
watch kubectl get -n kube-system pod -lapp.kubernetes.io/name=rke2-ingress-nginx
```

## K3s specific setup


Copy the `k3s-traefik-config.yaml` file to `/var/lib/rancher/k3s/server/manifests/` on your K3s node.
Wait for trafik to restart.
Run this commant to watch it restart:

```
watch kubectl get -n kube-system pod -lapp.kubernetes.io/name=traefik
```

# Offline installation


## For K3s

With K3s it is possible to preload the container images and avoid it to be fetched from a registry.
For this, on a machine with internet access, pull the image using `podman`, `docker` or `skopeo` and save it as a `tar` archive.
For example:

⚠️ **TODO**: Verify instructions
```
for image in cert-manager-cainjector cert-manager-controller cert-manager-ctl cert-manager-webhook; do
  podman pull quay.io/jetstack/$image
  podman save --output $image.tar quay.io/jetstack/$image:latest
done

podman pull registry.opensuse.org/systemsmanagement/uyuni/master/servercontainer/containers/uyuni/server:latest

podman save --output server.tar registry.opensuse.org/systemsmanagement/uyuni/master/servercontainer/containers/uyuni/server:latest
```

or

⚠️ **TODO**: Verify instructions
```
for image in cert-manager-cainjector cert-manager-controller cert-manager-ctl cert-manager-webhook; do
    skopeo copy docker://quay.io/jetstack/$image:latest docker-archive:$image.tar:quay.io/jetstack/$image:latest
done

skopeo copy docker://registry.opensuse.org/systemsmanagement/uyuni/master/servercontainer/containers/uyuni/server:latest docker-archive:server.tar:registry.opensuse.org/systemsmanagement/uyuni/master/servercontainer/containers/uyuni/server:latest
```

Copy the `cert-manager` and `uyuni/server` helm charts locally:

⚠️ **TODO**: verify instructions

```
helm pull --repo https://charts.jetstack.io --destination . cert-manager
helm pull --destination . oci://registry.opensuse.org/uyuni/server
```

Transfer the resulting `*.tar` images to the K3s node and load them using the following command:

```
for archive in `ls *.tar`; do
    k3s ctr images import $archive 
done
```

In order to tell K3s to not pull the images, set the image pull policy needs to be set to `Never`.
This needs to be done for both Uyuni and cert-manager helm charts.

For the Uyuni helm chart, set the `pullPolicy` chart value to `Never` by passing a `--helm-uyuni-values=uyuni-values.yaml` parameter to `uyuniadm install` with the following `uyuni-values.yaml` file content:

```
pullPolicy: Never
```

For the cert-manager helm chart, create a `cert-values.yaml` file with the following content and pass `--helm-certmanager-values=values.yaml` parameter to `uyuniadm install`:

```
image:
  pullPolicy: Never
```

⚠️ **TODO**: verify the file names
To use the downloaded helm charts instead of the default ones, pass `--helm-uyuni-chart=server.tgz` and `--helm-certmanager-chart=cert-manager.tgz` or add the following to the `uyuniadm` configuration file:

```
helm:
  uyuni:
    chart: server.tgz
    values: uyuni-values.yaml
  certmanager:
    chart: cert-manager.tgz
    values: cert.values.yaml
```

## For RKE2

RKE2 doesn't allow to preload images on the nodes.
Instead, use `skopeo` to import the images in a local registry and use this one to install.

Copy the `cert-manager` and `uyuni/server` helm charts locally:

⚠️ **TODO**: verify instructions

```
helm pull --repo https://charts.jetstack.io --destination . cert-manager
helm pull --destination . oci://registry.opensuse.org/uyuni/server
```

⚠️  **TODO** Prepare instructions
```
# TODO Copy the cert-manager and uyuni images
# TODO Set the uyuniadm parameters
```

## For Podman

With K3s it is possible to preload the container images and avoid it to be fetched from a registry.
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

Transfer the resulting `server-image.tar` to the server and load it using the following command:

```
podman load -i server-image.tar
```

# Migrating from a regular server

In order to migrate a regular Uyuni server to containers, a new machine is required: it is not possible to perform an in-place migration.
The old server is designated as the source server and the new machine is the destination one.

The migration procedure does not perform any hostname rename.
The fully qualified domain name will be the same on the new server than on the source one.
This means the DNS records need to be adjusted after the migration to use the new server.

## Preparing

### Stop the source server

Stop the source services:

```
spacewalk-service stop
systemctl stop postgresql
```

### Preparing the SSH connection

The `SSH` configuration and agent should be ready on the host for a password less connection to the source server.
The migration script only uses the source server fully qualified domain name in the SSH command.
This means that every other configuration required to connect needs to be defined in the `~/.ssh/config` file.

For a password less connection, the migration script will use an SSH agent on the server.
If none is running yet, run `eval $(ssh-agent)`.
Add the SSH key to the running agent using `ssh-add /path/to/the/private/key`.
The private key password will be prompted.

### Prepare for Kubernetes

Since the migration job will start the container from scratch the Persistent Volumes need to be defined before running the `uyuniadm migrate command`.
Refer to the installation section for more details on the volumes preparation.

## Migrating

Run the following command to install a new Uyuni server from the source one after replacing the `uyuni.source.fqdn` by the proper source server FQDN:
This command will synchronize all the data from the source server to the new one: this can take time!

```
uyuniadm migrate uyuni.source.fqdn
```

## Notes for Kubernetes

⚠️ **TODO** Revisit this section!

Once done, both the job and its pod will remain until the user deletes them to allow checking logs.

Certificates migration also needs to be documented, but that can be guessed for now with the instructions to setup a server from scratch.


# Installing Uyuni

## Volumes preparation 

### For Kubernetes

⚠️ **TODO** Document this

### For Podman

⚠️ **TODO** Document this

## Installing

The installation using `uyuniadm install` will ask for the password if those are not provided using the command line parameters or the configuration file.
For security reason, using command line parameters to specify passwords should be avoided: use the configuration file with proper permissions instead.

Prepare an `uyuniadm.yaml` file like the following:

```
db:
  password: MySuperSecretDBPass
cert:
  password: MySuperSecretCAPass
```

To dismiss the email prompts add the `email` and `emailFrom` configurations to the above file or use the `--email` and `--emailFrom` parameters for `uyuniadm install`.

Run the following command to install after replacing the `uyuni.example.com` by the FQDN of the server to install:

```
uyuniadm -c uyuniadm.yaml install --image registry.opensuse.org/systemsmanagement/uyuni/master/servercontainer/containers/uyuni/server uyuni.example.com
```

### Podman specific configuration

Additional parameters can be passed to Podman using `--podman-arg` parameters or configuration like the following in `uyuniadm.yaml`:

```
podman:
  arg:
    - -p 8000:8000
    - -p 8001:8001
```

is equivalent to passing `--podman-arg "-p 8000:8000" --podman-arg "-p 8001:8001"` to `uyuniadm install`

This can be usefull to expose ports like the Java debugging ones or mount additional volumes.

### Kubernetes specific configuration

The `uyuniadm install` command comes with parameters and thus configuration values for advanced helm chart configuration.
To pass additional values to the Uyuni helm chart at installation time, use the `--helm-uyuni-values chart-values.yaml` parameter or a configuration like the following:

```
helm:
  uyuni:
    values: chart-values.yaml
```

The path set as value for this configuration is a YAML file passed to the Uyuni Helm chart.
Be aware that some of the values in this file will be overriden by the `uyuniadm install` parameters.

For example, to expose the Java debugging ports, add the `exposeJavaDebug: true` line to the helm chart values file.
You can also set more variables like `sccUser` or `sccPass`.
Check the [server-helm/values.yaml](https://github.com/uyuni-project/uyuni/blob/server-container/containers/server-helm/values.yaml) file for the complete list.

If deploying on RKE2, add the `ingress: nginx` line to the Helm chart values file.

Note that the Helm chart installs a deployment with one replica.
The pod name is automatically generated by Kubernetes and changes at every start.


# Using Uyuni in containers

To getting a shell in the pod run `uyunictl exec -ti bash`.
Note that this command can be use to run any command to run inside the server like `uyunictl exec tail /var/log/rhn/rhn_web_ui.log`

To copy files to the server, use the `uyunictl cp <local_path> server:<remote_path>` command.
Conversely to copy files from the server use `uyunictl cp server:<remote_path> <local_path>`.

# Developping with the containers

##  Deploying code

To deploy java code on the pod change to the `java` directory and run:

```
ant -f manager-build.xml refresh-branding-jar deploy-restart-kube
```

In case you changed the pod namespace, pass the corresponding `-Ddeploy.namespace=<yourns>` parameter.

**Note** To deploy TSX or Salt code, use the `deploy-static-resources-kube` and `deploy-salt-files-kube` tasks of the ant file.

## Attaching a java debugger

First enable the JDWP options in both tomcat and taskomatic using the following command:

```
ant -f manager-build.xml enable-java-debug-kube
```

Then restart tomcat and taskomatic using ant too:

```
ant -f manager-build.xml restart-tomcat-kube restart-taskomatic-kube
```

The debugger can now be attached to the usual ports (8000 for tomcat and 8001 for taskomatic) on the host FQDN.

# Uninstalling

To remove everything including the volumes, run the following command:

```
uyuniadm uninstall --purge-volumes
```

Note that `cert-manager` will not be uninstalled if it was not installed by `uyuniadm`.
