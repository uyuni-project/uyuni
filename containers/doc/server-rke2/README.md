# Running the server-image on rke2

## Prerequisites

The following assumes you have a single-node rke2 cluster ready with enough resources for the Uyuni server.
It also assumes that `kubectl` is installed on your machine and configured to connect to the rke2 cluster.

** HACK ** For now I used the SSL certificates and CA generated in one of my installation attempts.
I will assume you already have SSL certificates matching the FQDN of the cluster node.
Instructions or tools on how to generate those will come later.

## Setting up the resources

Create the TLS secret holding the server SSL certificates:

```
kubectl create secret tls uyuni-cert --key <pathto>/server.key --cert <pathto>/server.crt
```

Create a `ConfigMap` with the CA certificate:

```
kubectl create configmap uyuni-ca --from-file=ca.crt=<pathto>/RHN-ORG-TRUSTED-SSL-CERT
```

The volumes are folders on the cluster node.
They need to be manually created:

```
for VOLUME in apache2 etc-rhn etc-salt etc-systemd etc-tls pgsql root srv-salt tomcat var-cache var-log-rhn var-spacewalk; do
    mkdir /var/uyuni/$VOLUME
done
```

In my setup, the cluster node is named `uyuni-dev` and its FQDN is `uyuni-dev.world-co.com`.
You will need to replace those values in the yaml files.

Copy the `rke2-ingress-nginx-config.yaml` file to `/var/lib/rancher/rke2/server/manifests/rke2-ingress-nginx-config.yaml` on your rke2 node.
Wait for the ingress controller to restart.
Run this command to watch it restart:

```
watch kubectl get -n kube-system pod -lapp.kubernetes.io/name=rke2-ingress-nginx
```

Once done, run the following commands:

```
for YAML in pvs pvcs service uyuni-config server uyuni-ingress; do
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

To deploy java code on the pod change to the `java` directory and run:

```
ant -f manager-build.xml refresh-branding-jar deploy-restart-kube
```

In case you changed the pod name and namespace while deploying it, pass the corresponding `-Ddeploy.namespace=<yourns>` and `-Ddeploy.pod=<yourpod>` parameters.

**Note** To deploy TSX or Salt code, use the `deploy-static-resources-kube` and `deploy-salt-files-kube` tasks of the ant file.


## Throwing everything away

If you want to create from a fresh pod, run `kubectl delete pod uyuni`.

Then run this command on the cluster node to cleanup the volumes:

```
for v in `ls /var/uyuni/`; do
    rm -r /var/uyuni/$v; mkdir /var/uyuni/$v
done
```

To create the pod again, just run `kubectl apply -f server.yaml` and wait.
