# Prerequisites

The following assumes you have either a single-node RKE2 or K3s cluster ready or a server with Podman installed and enough resources for the Uyuni server.
When installing on a Kubernetes cluster, it also assumes that `kubectl` and `helm` are installed on the server and configured to connect to the cluster.

Note that in the case of a k3s or rke2 cluster the kubeconfig will be discovered in the default `/etc/rancher` folder: there is no need to set `KUBECONFIG` or copy the file to `~/.kube/config`.

# Preparing the installation

## Podman specific setup

Podman stores its volumes in `/var/lib/containers/storage/volumes/`.
In order to provide custom storage for the volumes, mount disks on that path or even the expected volume path inside it like `/var/lib/containers/storage/volumes/var-spacewalk`.

**This needs to be performed before installing Uyuni as the volumes will be populated at that time.**

## RKE2 specific setup

RKE2 doesn't have automatically provisioning Persistent Volume by default.
Either the expected Persistent Volumes need to be created before hand or a storage class with automatic provisioning has to be defined before installing Uyuni.

## K3s specific setup

The installation will work perfectly fine without changing anything, but tuning the storage class may be needed to avoid using the local path provisioner.

# Offline installation

## For K3s

In the following instructions the cert-manager images and charts need to be pulled and used only if third party SSL server certificate will not be provided.

With K3s it is possible to preload the container images and avoid it to be fetched from a registry.
For this, on a machine with internet access, pull the image using `podman`, `docker` or `skopeo` and save it as a `tar` archive.
For example:

```bash
cert_manager_version=$(helm show chart --repo https://charts.jetstack.io/ cert-manager | grep '^version:' | cut -f 2 -d ' ')
for image in cert-manager-cainjector cert-manager-controller cert-manager-ctl cert-manager-webhook; do
  podman pull quay.io/jetstack/$image:$cert_manager_version
  podman save --output $image.tar quay.io/jetstack/$image:$cert_manager_version
done

podman pull registry.opensuse.org/uyuni/server:latest

podman save --output server.tar registry.opensuse.org/uyuni/server:latest
```

or

```bash
cert_manager_version=$(helm show chart --repo https://charts.jetstack.io/ cert-manager | grep '^version:' | cut -f 2 -d ' ')
for image in cert-manager-cainjector cert-manager-controller cert-manager-ctl cert-manager-webhook; do
    skopeo copy docker://quay.io/jetstack/$image:$cert_manager_version docker-archive:$image.tar:quay.io/jetstack/$image:$cert_manager_version
done

skopeo copy docker://registry.opensuse.org/uyuni/server:latest docker-archive:server.tar:registry.opensuse.org/uyuni/server:latest
```

If using K3S's default local-path-provider, also pull the helper pod image for offline use:
Run the following command on the K3S node to find out the name of the image to pull:

```bash
grep helper-pod -A1 /var/lib/rancher/k3s/server/manifests/local-storage.yaml | grep image | sed 's/^ \+image: //'
```

Then set the `helper_pod_image` variable with the returned output on the machine having internet access and run the next commands to pull the image:

```bash
podman pull $helper_pod_image
podman save --output helper_pod.tar $helper_pod_image
```

or

```bash
skopeo copy docker://$(helper_pod_image) docker-archive:helper-pod.tar:$(helper_pod_image)
```

Copy the `cert-manager` and `uyuni/server` helm charts locally:

```bash
helm pull --repo https://charts.jetstack.io --destination . cert-manager
helm pull --destination . oci://registry.opensuse.org/uyuni/server-helm
```

Transfer the resulting `*.tar` images to the K3s node and load them using the following command:

```bash
for archive in `ls *.tar`; do
    k3s ctr images import $archive 
done
```

In order to tell K3s to not pull the images, set the image pull policy needs to be set to `Never`.
This needs to be done for both Uyuni and cert-manager helm charts.

To prevent Helm from pulling the images pass the `--image-pullPolicy=never` parameter to `mgradm install` or `mgradm migrate`.

To use the downloaded helm charts instead of the default ones, pass `--helm-uyuni-chart=server-helm-2023.10.0.tgz` and `--helm-certmanager-chart=cert-manager-v1.13.1.tgz` or add the following to the `mgradm` configuration file. Of course the versions in the file name need to be adjusted to what you downloaded:

```yaml
helm:
  uyuni:
    chart: server-helm-2023.10.0.tgz
  certmanager:
    chart: cert-manager-v1.13.1.tgz
```

If using K3S's default local-path-provisioner, set the helper-pod `imagePullPolicy` to `Never` in `/var/lib/rancher/k3s/server/manifests/local-storage.yaml` using the following command:

```bash
sed 's/imagePullPolicy: IfNotPresent/imagePullPolicy: Never/' -i /var/lib/rancher/k3s/server/manifests/local-storage.yaml
```

## For RKE2

Just like for K3S, cert-manager images and chart do not need to be copied if a third party SSL server certificate is to be used.

RKE2 doesn't allow to preload images on the nodes.
Instead, use `skopeo` to import the images in a local registry and use this one to install.

Copy the `cert-manager` and `uyuni/server-helm` helm charts locally:

```bash
helm pull --repo https://charts.jetstack.io --destination . cert-manager
helm pull --destination . oci://registry.opensuse.org/uyuni/server-helm
```

⚠️  **TODO** Prepare instructions
```
# TODO Copy the cert-manager and uyuni images
# TODO Set the mgradm parameters
```

## For Podman

With Podman it is possible to preload the container images and avoid it to be fetched from a registry.
For this, on a machine with internet access, pull the image using `podman`, `docker` or `skopeo` and save it as a `tar` archive.
For example:

```bash
podman pull registry.opensuse.org/uyuni/server:latest
podman save --output server.tar registry.opensuse.org/uyuni/server:latest
```

or

```bash
skopeo copy docker://registry.opensuse.org/uyuni/server:latest docker-archive:server.tar:registry.opensuse.org/uyuni/server:latest
```

Transfer the resulting `server-image.tar` to the server and load it using the following command:

```bash
podman load -i server.tar
```

To prevent pulling the images pass the `--image-pullPolicy=never` parameter to `mgradm install` or `mgradm migrate`.

# Migrating from a regular server

In order to migrate a regular Uyuni server to containers, a new machine is required: it is not possible to perform an in-place migration.
The old server is designated as the source server and the new machine is the destination one.

The migration procedure does not perform any hostname rename.
The fully qualified domain name will be the same on the new server than on the source one.
This means the DNS records need to be adjusted after the migration to use the new server.

## Preparing

### Stop the source server

Stop the source services:

```bash
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

### In case SELinux is enabled

Verify that SELinux is enabled on the new host by running `getenforce`. If the output is `Enforcing`, SELinux is enabled.
In such case, we temporarily provide [this custom policy](uyuni-selinux-policy.cil) to allow the migration script to run. This file needs to be copied to `/root/uyuni-selinux-policy.cil` on the new host.
The migration script will load the policy into the new host and pass it as podman argument to run the migration script.

In the future, we plan to ship this custom policy packaged in a RPM and this step will not be required anymore.

### Prepare for Kubernetes

Since the migration job will start the container from scratch the Persistent Volumes need to be defined before running the `mgradm migrate command`.
Refer to the installation section for more details on the volumes preparation.

## Migrating

Run the following command to install a new Uyuni server from the source one after replacing the `uyuni.source.fqdn` by the proper source server FQDN:
This command will synchronize all the data from the source server to the new one: this can take time!

```bash
mgradm migrate podman uyuni.source.fqdn
```

or

```bash
mgradm migrate kubernetes uyuni.source.fqdn
```

# Installing Uyuni

## Installing

The installation using `mgradm install` will ask for the password if those are not provided using the command line parameters or the configuration file.
For security reason, using command line parameters to specify passwords should be avoided: use the configuration file with proper permissions instead.

Prepare an `mgradm.yaml` file like the following:

```yaml
db:
  password: MySuperSecretDBPass
cert:
  password: MySuperSecretCAPass
```

To dismiss the email prompts add the `email` and `emailFrom` configurations to the above file or use the `--email` and `--emailFrom` parameters for `mgradm install`.

Run one of the following command to install after replacing the `uyuni.example.com` by the FQDN of the server to install:

```bash
mgradm -c mgradm.yaml install podman uyuni.example.com
```

or

```bash
mgradm -c mgradm.yaml install kubernetes uyuni.example.com
```

### Podman specific configuration

Additional parameters can be passed to Podman using `--podman-arg` parameters.

### Kubernetes specific configuration

The `mgradm install` command comes with parameters and thus configuration values for advanced helm chart configuration.
To pass additional values to the Uyuni helm chart at installation time, use the `--helm-uyuni-values chart-values.yaml` parameter or a configuration like the following:

```yaml
helm:
  uyuni:
    values: chart-values.yaml
```

The path set as value for this configuration is a YAML file passed to the Uyuni Helm chart.
Be aware that some of the values in this file will be overridden by the `mgradm install` parameters.

Note that the Helm chart installs a deployment with one replica.
The pod name is automatically generated by Kubernetes and changes at every start.

# Using Uyuni in containers

To get a shell in the pod run `mgrctl exec -ti bash`.
Note that this command can be used to run any command inside the server like `mgrctl exec tail /var/log/rhn/rhn_web_ui.log`

To copy files to the server, use the `mgrctl cp <local_path> server:<remote_path>` command.
Conversely to copy files from the server use `mgrctl cp server:<remote_path> <local_path>`.

# Developping with the containers

## Deploying code

To deploy java code on the pod change to the `java` directory and run:

```bash
ant -f manager-build.xml refresh-branding-jar deploy-restart-container
```

**Note** To deploy TSX or Salt code, use the `deploy-static-resources-container` and `deploy-salt-files-container` tasks of the ant file.

## Attaching a java debugger

In order to attach a Java debugger Uyuni need to have been installed using the `--debug-java` option to setup the container to listen on JDWP ports and expose them.

The debugger can now be attached to the usual ports (`8003` for tomcat and `8001` for Taskomatic and `8002` for the search server) on the host FQDN.

# Uninstalling

To remove everything including the volumes, run the following command:

```bash
mgradm uninstall --purge-volumes
```

Note that `cert-manager` will not be uninstalled if it was not installed by `mgradm`.
