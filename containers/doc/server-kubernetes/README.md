# Prerequisites

The following assumes you have either a single-node RKE2 or K3s cluster ready or a server with Podman installed and enough resources for the Uyuni server.
When installing on a Kubernetes cluster, it also assumes that `kubectl` and `helm` are installed on the server and configured to connect to the cluster.

Note that in the case of a k3s or rke2 cluster the kubeconfig will be discovered in the default `/etc/rancher` folder: there is no need to set `KUBECONFIG` or copy the file to `~/.kube/config`.

# Preparing the installation

## Podman specific setup

Podman stores its volumes in `/var/lib/containers/storage/volumes/`.
In order to provide custom storage for the volumes, mount disks on that path oreven the expected volume path inside it like `/var/lib/containers/storage/volumes/var-spacewalk`. 

**This needs to be performed before installing Uyuni as the volumes will be populated at that time.**

## RKE2 specific setup

RKE2 doesn't have automatically provisioning Persistent Volume by default.
Either the expected Persisten Volumes need to be created before hand or a storage class with automatic provisioning has to be defined before installing Uyuni.

## K3s specific setup

The installation will work perfectly fine without changing anything, but tuning the storage class may be needed to avoid using the local path provisioner.

# Offline installation

## For K3s

In the following instructions the cert-manager images and charts need to be pulled and used only if third party SSL server certificate will not be provided.

With K3s it is possible to preload the container images and avoid it to be fetched from a registry.
For this, on a machine with internet access, pull the image using `podman`, `docker` or `skopeo` and save it as a `tar` archive.
For example:

```
cert_manager_version=$(helm show chart --repo https://charts.jetstack.io/ cert-manager | grep '^version:' | cut -f 2 -d ' ')
for image in cert-manager-cainjector cert-manager-controller cert-manager-ctl cert-manager-webhook; do
  podman pull quay.io/jetstack/$image:$cert_manager_version
  podman save --output $image.tar quay.io/jetstack/$image:$cert_manager_version
done

podman pull registry.opensuse.org/systemsmanagement/uyuni/master/containers/uyuni/server:latest

podman save --output server.tar registry.opensuse.org/systemsmanagement/uyuni/master/containers/uyuni/server:latest

helper_pod_image=$(grep helper-pod -A1 /var/lib/rancher/k3s/server/manifests/local-storage.yaml | grep image | sed 's/^ \+image: //')
podman pull $helper_pod_image
podman save --output helper_pod.tar $helper_pod_image
```


or

```
cert_manager_version=$(helm show chart --repo https://charts.jetstack.io/ cert-manager | grep '^version:' | cut -f 2 -d ' ')
for image in cert-manager-cainjector cert-manager-controller cert-manager-ctl cert-manager-webhook; do
    skopeo copy docker://quay.io/jetstack/$image:$cert_manager_version docker-archive:$image.tar:quay.io/jetstack/$image:$cert_manager_version
done

skopeo copy docker://registry.opensuse.org/systemsmanagement/uyuni/master/containers/uyuni/server:latest docker-archive:server.tar:registry.opensuse.org/systemsmanagement/uyuni/master/containers/uyuni/server:latest
```

Copy the `cert-manager` and `uyuni/server` helm charts locally:

```
helm pull --repo https://charts.jetstack.io --destination . cert-manager
helm pull --destination . oci://registry.opensuse.org/systemsmanagement/uyuni/master/charts/uyuni/server
```

Transfer the resulting `*.tar` images to the K3s node and load them using the following command:

```
for archive in `ls *.tar`; do
    k3s ctr images import $archive 
done
```

In order to tell K3s to not pull the images, set the image pull policy needs to be set to `Never`.
Add the `--image-pullPolicy=Never` parameter to `uyuniadm` command or add the following to the configuration file:

```
image:
  pullPolicy: Never
```

To use the downloaded helm charts instead of the default ones, pass `--helm-uyuni-chart=server-2023.9.0.tgz` and `--helm-certmanager-chart=cert-manager-v1.13.1.tgz` or add the following to the `uyuniadm` configuration file. Of course the versions in the file name need to be adjusted to what you downloaded:

```
helm:
  uyuni:
    chart: server-2023.9.0.tgz
    values: uyuni-values.yaml
  certmanager:
    chart: cert-manager-v1.13.1.tgz
    values: cert-values.yaml
```

Set the helper-pod `imagePullPolicy` to `Never` in `/var/lib/rancher/k3s/server/manifests/local-storage.yaml` using the following command:

```
sed 's/imagePullPolicy: IfNotPresent/imagePullPolicy: Never/' -i /var/lib/rancher/k3s/server/manifests/local-storage.yaml
```

## For RKE2

Just like for K3S, cert-manager images and chart do not need to be copied if a third party SSL server certificate is to be used.

RKE2 doesn't allow to preload images on the nodes.
Instead, use `skopeo` to import the images in a local registry and use this one to install.

Copy the `cert-manager` and `uyuni/server` helm charts locally:

```
helm pull --repo https://charts.jetstack.io --destination . cert-manager
helm pull --destination . oci://registry.opensuse.org/systemsmanagement/uyuni/master/charts/uyuni/server
```

⚠️  **TODO** Prepare instructions
```
# TODO Copy the cert-manager and uyuni images
# TODO Set the uyuniadm parameters
```

## For Podman

With Podman it is possible to preload the container images and avoid it to be fetched from a registry.
For this, on a machine with internet access, pull the image using `podman`, `docker` or `skopeo` and save it as a `tar` archive.
For example:

```
podman pull registry.opensuse.org/systemsmanagement/uyuni/master/containers/uyuni/server:latest
podman save --output server.tar registry.opensuse.org/systemsmanagement/uyuni/master/containers/uyuni/server:latest
```

or

```
skopeo copy docker://registry.opensuse.org/systemsmanagement/uyuni/master/containers/uyuni/server:latest docker-archive:server.tar:registry.opensuse.org/systemsmanagement/uyuni/master/containers/uyuni/server:latest
```

Transfer the resulting `server-image.tar` to the server and load it using the following command:

```
podman load -i server.tar
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

The `SSH` configuration and agent should be ready on the host for a passwordless connection to the source server.
The migration script only uses the source server fully qualified domain name in the SSH command.
This means that every other configuration required to connect needs to be defined in the `~/.ssh/config` file.

For a passwordless connection, the migration script will use an SSH agent on the server.
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
uyuniadm migrate podman uyuni.source.fqdn
```

or

```
uyuniadm migrate kubernetes uyuni.source.fqdn
```

# Installing Uyuni

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

Run one of the following command to install after replacing the `uyuni.example.com` by the FQDN of the server to install:

```
uyuniadm -c uyuniadm.yaml install podman uyuni.example.com
```

or

```
uyuniadm -c uyuniadm.yaml install kubernetes uyuni.example.com
```

### Podman specific configuration

Additional parameters can be passed to Podman using `--podman-arg` parameters.

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

Note that the Helm chart installs a deployment with one replica.
The pod name is automatically generated by Kubernetes and changes at every start.


# Using Uyuni in containers

To get a shell in the pod run `uyunictl exec -ti bash`.
Note that this command can be used to run any command inside the server like `uyunictl exec tail /var/log/rhn/rhn_web_ui.log`

To copy files to the server, use the `uyunictl cp <local_path> server:<remote_path>` command.
Conversely to copy files from the server use `uyunictl cp server:<remote_path> <local_path>`.

# Developping with the containers

##  Deploying code

To deploy java code on the pod change to the `java` directory and run:

```
ant -f manager-build.xml refresh-branding-jar deploy-restart-container
```

**Note** To deploy TSX or Salt code, use the `deploy-static-resources-container` and `deploy-salt-files-container` tasks of the ant file.

## Attaching a java debugger

In order to attach a Java debugger Uyuni need to have been installed using the `--debug-java` option to setup the container to listen on JDWP ports and expose them.

The debugger can now be attached to the usual ports (8003 for tomcat and 8001 for taskomatic and 8002 for the search server) on the host FQDN.

# Uninstalling

To remove everything including the volumes, run the following command:

```
uyuniadm uninstall --purge-volumes
```

Note that `cert-manager` will not be uninstalled if it was not installed by `uyuniadm`.
