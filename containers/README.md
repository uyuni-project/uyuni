## Install on k3s with MetalLB

### Installing k3s

On the proxy host machine, install `k3s` without the load balancer and Traefik router:

```bash
curl -sfL https://get.k3s.io | INSTALL_K3S_EXEC="--disable=traefik --disable=servicelb --tls-san=<K3S_HOST_FQDN>" sh -
```

### Configuring cluster access

`helm` needs a configuration file to connect to the target kubernetes cluster.
This file is usually called a `kubeconfig`

On the cluster server machine run the following command
You can optionally transfer the resulting `kubeconfig-k3s.yaml` to your work machine:

```bash
kubectl config view --flatten=true | sed 's/127.0.0.1/<K3S_HOST_FQDN>/' >kubeconfig-k3s.yaml
```

Before calling `helm`, run `export KUBECONFIG=/path/to/kubeconfig-k3s.yaml`.

### Installing helm

On a SUSE Linux Enterprise Server machine, the **Containers Module** is required to install `helm`.
Simply run:

```bash
zypper in helm
```

### Installing metalLB

MetalLB is the LoadBalancer that will expose the proxy pod services to the outside world.
To install it, run:

```bash
helm repo add metallb https://metallb.github.io/metallb
helm install --create-namespace -n metallb metallb metallb/metallb 
```

MetalLB still requires a configuration to know the virtual IP address range to be used.
In this example, the virtual IP addresses will be from `192.168.122.240` to `192.168.122.250`, but we could lower that range since only one address will be used in the end.
This addresses obviously need to be a subset of the server network.

Create a `metallb-config.yaml` with content like the following with an IP address range that aligns with the deployed network:

```yaml
apiVersion: metallb.io/v1beta1
kind: IPAddressPool
metadata:
  name: l2-pool
  namespace: metallb
spec:
  addresses:
  - 192.168.122.240-192.168.122.250
---
apiVersion: metallb.io/v1beta1
kind: L2Advertisement
metadata:
  name: l2 
  namespace: metallb
spec:
  ipAddressPools:
  - l2-pool
```

Apply this configuration by running:

```bash
kubectl apply -f metallb-config.yaml
```

### Deploying the proxy helm chart

Before deploying the proxy containers, we need to add a configuration file forcing the IP address MetalLB will use for the proxy services.
This IP address needs to be the one to which the proxy FQDN entered when creating the proxy configuration.
This example will use `192.168.122.241`.

Create a `custom-values.yaml` file with the following content:

```yaml
services:
  annotations:
    metallb.universe.tf/allow-shared-ip: key-to-share-ip
    metallb.universe.tf/loadBalancerIPs: 192.168.122.241
```

If you want to configure the storage of the volumes to be used by the proxy pod, define persistent volumes for the following claims.
Please refer to the [Kubernetes documentation](https://kubernetes.io/docs/concepts/storage/persistent-volumes/) for more details.

* default/squid-cache-pv-claim
* default/package-cache-pv-claim
* default/tftp-boot-pv-claim

Copy and extract the proxy configuration file and then deploy the proxy helm chart:

```bash
tar xf /path/to/config.tar.gz
helm install uyuni-proxy oci://registry.opensuse.org/uyuni/proxy -f config.yaml -f httpd.yaml -f ssh.yaml -f custom-values.yaml
```

To install the helm chart from SUSE Manager, use the `oci://registry.suse.com/suse/manager/4.3/proxy` URL instead.
