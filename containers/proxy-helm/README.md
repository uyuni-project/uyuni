# proxy-helm

This chart installs [Uyuni proxy](https://uyuni-project.org).
This is deploying the core features, [the uyuni-charts repository](https://github.com/uyuni-project/uyuni-charts) contains some examples Helm charts using it in conjunction with other tools.

## Configuration

### Secrets and CA ConfigMap

The proxy needs a TLS secret `proxy-cert` (its server certificate + private key) and a ConfigMap `uyuni-ca` (root CA in the `ca.crt` key).

The chart will **create both automatically** when the source data is present in the supplied tarball values:

- `proxy-cert` is rendered when `httpd.yaml` contains `httpd.server_crt` and `httpd.server_key`
- `uyuni-ca` is rendered when `config.yaml` contains `ca_crt`

Both server-generated fields contain an OpenSSL human-readable text dump followed by the PEM block; the chart strips the dump and keeps only the PEM block.

If those fields are absent from the tarball, the chart renders nothing and the objects must be provided as prerequisites:

```bash
kubectl -n <namespace> create secret tls proxy-cert \
  --cert=server.crt --key=server.key

kubectl -n <namespace> create configmap uyuni-ca \
  --from-file=ca.crt=ca.crt
```

#### Pre-existing manually-created objects

If `proxy-cert` or `uyuni-ca` already exist in the namespace (e.g. created by hand on a previous install), the chart leaves them alone by default — it will not render over them, and `helm install`/`upgrade` will not fail. The proxy keeps using the values already present.

To let helm take over and manage them from the tarball values, the chart has to render the resources so helm has something to adopt. Set `cert.takeOwnership=true` and pass `helm install --take-ownership` (helm 3.14+) once:

```bash
helm upgrade --install uyuni-proxy <chart> \
  --namespace <namespace> \
  --take-ownership \
  --set cert.takeOwnership=true \
  --set-file global.config=config.yaml \
  --set-file global.httpd=httpd.yaml \
  --set-file global.ssh=ssh.yaml
```

After adoption the chart treats them as helm-managed: value changes are applied on upgrade, and they are removed on `helm uninstall` like any other chart resource. `cert.takeOwnership` can be left at `true` or dropped back to `false` on subsequent upgrades — once the objects carry the helm `managed-by` label the default render condition already covers them.

### Persistent Volumes

The following persistent volume claims will be created and will need to be bound to persistent volumes.

- `squid-cache`: the size it taken from the generated configuration

They all are using the `ReadWriteOnce` access mode and can be configured in the values.

Changing the default volume sizes according to the distributions you plan to synchronize and manage is recommended.
See the [requirements documentation](https://www.uyuni-project.org/uyuni-docs/en/uyuni/installation-and-upgrade/uyuni-install-requirements.html#_proxy_requirements) for more information.

### Node Tuning

For the Uyuni Proxy deployments, it is possible to finely control which nodes the pods are scheduled on. This chart supports a **default** configuration with **local overrides**, allowing baseline rules to be set for all pods and customized for specific components (like `tftp` or the main proxy) when needed.

Scheduling can be controlled using `nodeSelector`, `affinity`, `tolerations`, or `nodeName`. It is not necessary to use all of them; simply choose the method that matches the cluster's scheduling strategy.

> Note: These keys are **not pre-populated** in the default `values.yaml`. They can be added under the sections below (e.g. `placement:`, `proxy:`, `tftp:`) as needed for the deployment.

For example, to set a baseline rule for all components but override the placement for the `proxy` and `tftp` pods specifically, the `values.yaml` could look like this:

```yaml
# DEFAULTS
# These rules apply to all pods unless overridden by a specific component.
placement:
  nodeSelector:
    environment: production

  # Allowing all pods to schedule on tainted nodes
  tolerations:
  - key: "proxy-tier"
    operator: "Equal"
    value: "true"
    effect: "NoSchedule"

# LOCAL OVERRIDES
# These rules apply ONLY to the specific component and override the global equivalents.
proxy:
  # Example: ensure the main proxy pods land on a specific node pool
  nodeSelector:
    node-pool: "proxy-nodes"

tftp:
  # Overrides the global nodeSelector with a specific node requirement
  nodeSelector:
    "kubernetes.io/hostname": "node-42"

  # Complex scheduling rules (Soft or Hard requirements)
  affinity:
    nodeAffinity:
      requiredDuringSchedulingIgnoredDuringExecution:
        nodeSelectorTerms:
        - matchExpressions:
          - key: "kubernetes.io/hostname"
            operator: In
            values:
            - "node-42"

  # Direct node assignment (bypasses the scheduler and affinity rules completely)
  # nodeName: "node-42"
```

### Exposing ports

Uyuni proxy requires some TCP ports to be routed to its services.
Here is a list of the ports to map:


| Protocol | Port  | Service name | Service port |
| -------- | ----- | ------------ | ------------ |
| TCP      | 8022  | ssh          | 8022         |
| TCP      | 4505  | salt         | 4505         |
| TCP      | 4506  | salt         | 4506         |


Exposing the `tftp` service has to be done differently due to the way TFTP protocol is working.
Either use the host network using the `tftp.hostNetwork` value or configure a load balancer for the `tftp` service.
Note that not all load balancers will work: `serviceLB` implementation is not compatible with TFTP protocol, while MetalLB works.

When using Traefik as the ingress, the three TCP services are routed through `IngressRouteTCP` resources bound to Traefik entryPoints. The chart defaults are `ssh`, `salt-publish` and `salt-request`. If the surrounding Traefik installation uses different entryPoint names, override them:

```yaml
ingress:
  entryPoints:
    ssh: "uyuni-ssh"
    saltPublish: "uyuni-publish"
    saltRequest: "uyuni-request"
```

### DNS configuration

The `dnsConfig` value is a passthrough to the pod's [Kubernetes `PodDNSConfig`](https://kubernetes.io/docs/concepts/services-networking/dns-pod-service/#pod-dns-config). It is unset by default so the pod inherits the cluster's normal DNS behavior.

If external FQDN lookups from the proxy hang for ~10s (the default `urlopen` timeout), the cluster's default `ndots:5` is likely expanding the name through several cluster search domains before falling back to the bare lookup. Override with:

```yaml
dnsConfig:
  options:
    - name: ndots
      value: "1"
```

### Configuration files

The chart requires the `global.config`, `global.httpd` and `global.ssh` values to contain the YAML produced by the server. Generate them from the **Systems > Proxy Configuration** page on the Uyuni/MLM server (or via the `proxy.containerConfig` API). The server returns a `config.tar.gz` containing `config.yaml`, `httpd.yaml` and `ssh.yaml`. Extract it and pass the files to Helm with `--set-file`.

## Installation

### RKE2

RKE2 ships Traefik as the default ingress controller. The chart's default `ingress.class: traefik` and entryPoint names (`ssh`, `salt-publish`, `salt-request`) must match the Traefik configuration. Drop a `HelmChartConfig` under `/var/lib/rancher/rke2/server/manifests/` to expose the required TCP entryPoints:

```yaml
apiVersion: helm.cattle.io/v1
kind: HelmChartConfig
metadata:
  name: rke2-traefik
  namespace: kube-system
spec:
  valuesContent: |-
    ports:
      ssh:
        port: 8022
        expose:
          default: true
        exposedPort: 8022
        protocol: TCP
        hostPort: 8022
      salt-publish:
        port: 4505
        expose:
          default: true
        exposedPort: 4505
        protocol: TCP
        hostPort: 4505
        containerPort: 4505
      salt-request:
        port: 4506
        expose:
          default: true
        exposedPort: 4506
        protocol: TCP
        hostPort: 4506
        containerPort: 4506
```

```bash
# 1. Extract the config tarball generated by the server
mkdir -p /root/proxy-config && cd /root/proxy-config
tar xzf /root/config.tar.gz   # gives config.yaml, httpd.yaml, ssh.yaml

# 2. Install the chart — proxy-cert and uyuni-ca are created from the tarball values
CHART=oci://<registry>/proxy   # adjust to your registry
kubectl create namespace uyuni-proxy
helm upgrade --install uyuni-proxy $CHART \
  --namespace uyuni-proxy \
  --set-file global.config=config.yaml \
  --set-file global.httpd=httpd.yaml \
  --set-file global.ssh=ssh.yaml \
  --set ingress.type=traefik \
  --set ingress.class=traefik
```

### K3S

K3S ships Traefik in the `kube-system` namespace. Two things need to be aligned with the chart before the proxy will work:

1. **`ingress.class`**: on a vanilla K3S, the bundled Traefik picks up every Ingress regardless of class, so `ingress.class: ""` is the safest value. If your Traefik is started with `--providers.kubernetescrd.ingressclass=traefik` (e.g. mgrpxy-customized installs), set `ingress.class: "traefik"` instead so the chart emits the matching annotation.
2. **Traefik entryPoints**: the bundled Traefik must expose TCP entryPoints whose names match `ingress.entryPoints` (defaults `ssh`, `salt-publish`, `salt-request`). Drop a `HelmChartConfig` under `/var/lib/rancher/k3s/server/manifests/` so K3S reconciles them automatically:

```yaml
apiVersion: helm.cattle.io/v1
kind: HelmChartConfig
metadata:
  name: traefik
  namespace: kube-system
spec:
  valuesContent: |-
    ports:
      ssh:
        port: 8022
        exposedPort: 8022
        protocol: TCP
        expose:
          default: true
      salt-publish:
        port: 4505
        exposedPort: 4505
        protocol: TCP
        expose:
          default: true
      salt-request:
        port: 4506
        exposedPort: 4506
        protocol: TCP
        expose:
          default: true
```

K3S's built-in load balancer (`klipper-lb` / `serviceLB`) does not support the TFTP protocol. Expose TFTP via the host network instead:

```yaml
tftp:
  hostNetwork: true
```

```bash
# 1. Extract the config tarball generated by the server
mkdir -p /root/proxy-config && cd /root/proxy-config
tar xzf /root/config.tar.gz   # gives config.yaml, httpd.yaml, ssh.yaml

# 2. Install the chart — proxy-cert and uyuni-ca are created from the tarball values
CHART=oci://<registry>/proxy   # adjust to your registry
kubectl create namespace uyuni-proxy
helm upgrade --install uyuni-proxy $CHART \
  --namespace uyuni-proxy \
  --set-file global.config=config.yaml \
  --set-file global.httpd=httpd.yaml \
  --set-file global.ssh=ssh.yaml \
  --set ingress.type=traefik \
  --set ingress.class=traefik \
  --set tftp.hostNetwork=true
```

## Upgrade from 5.1 to 5.2 on K3S

The deployment selector labels changed between 5.1 and 5.2 (`app: uyuni-proxy` → `app.kubernetes.io/component: proxy`). Since `spec.selector` is immutable in Kubernetes, the old deployment must be removed before the new one can be created. This causes a short downtime (~30-60s). The squid cache PVC can be preserved across the upgrade.

### Preparation (while 5.1 is still running)

```bash
# 1. Preserve the squid PVC by switching the PV reclaim policy to Retain
PV=$(kubectl -n <namespace> get pvc squid-cache -o jsonpath='{.spec.volumeName}')
kubectl patch pv $PV -p '{"spec":{"persistentVolumeReclaimPolicy":"Retain"}}'

# 2. Extract config files from the running 5.1 proxy
kubectl -n <namespace> exec deploy/uyuni-proxy -c httpd -- cat /etc/uyuni/config.yaml > /root/proxy-config/config.yaml
kubectl -n <namespace> exec deploy/uyuni-proxy -c httpd -- cat /etc/uyuni/httpd.yaml > /root/proxy-config/httpd.yaml
kubectl -n <namespace> exec deploy/uyuni-proxy -c ssh  -- cat /etc/uyuni/ssh.yaml   > /root/proxy-config/ssh.yaml

# 3. Prepare the new namespace — the chart will create proxy-cert and uyuni-ca
#    from the tarball values at install time.
kubectl create namespace uyuni-proxy
```

### Update Traefik (no downtime)

Replace the mgrpxy-managed Traefik config (which used `uyuni-ssh`, `uyuni-publish`, `uyuni-request`) with the 5.2 chart defaults:

```bash
cat > /var/lib/rancher/k3s/server/manifests/uyuni-traefik-config.yaml << 'EOF'
apiVersion: helm.cattle.io/v1
kind: HelmChartConfig
metadata:
  name: traefik
  namespace: kube-system
spec:
  valuesContent: |-
    ports:
      ssh:
        port: 8022
        exposedPort: 8022
        protocol: TCP
        expose:
          default: true
      salt-publish:
        port: 4505
        exposedPort: 4505
        protocol: TCP
        expose:
          default: true
      salt-request:
        port: 4506
        exposedPort: 4506
        protocol: TCP
        expose:
          default: true
EOF

kubectl -n kube-system rollout restart deploy/traefik
kubectl -n kube-system rollout status deploy/traefik
```

### Cutover (downtime starts here)

```bash
# 1. Uninstall 5.1
helm -n <namespace> uninstall <release-name>

# 2. Release the PV from its old claim so the new chart can bind to it
kubectl patch pv $PV --type=json \
  -p '[{"op":"remove","path":"/spec/claimRef"}]'

# 3. Install 5.2, binding to the existing squid PV
CHART=oci://<registry>/proxy
helm upgrade --install uyuni-proxy $CHART \
  --namespace uyuni-proxy \
  --set-file global.config=/root/proxy-config/config.yaml \
  --set-file global.httpd=/root/proxy-config/httpd.yaml \
  --set-file global.ssh=/root/proxy-config/ssh.yaml \
  --set ingress.type=traefik \
  --set ingress.class=traefik \
  --set tftp.hostNetwork=true \
  --set volumes.squid.volumeName=$PV
```

### Verify

```bash
kubectl -n uyuni-proxy get pod -w
kubectl -n uyuni-proxy get pvc   # squid-cache should be Bound to the old PV
kubectl -n uyuni-proxy logs deploy/uyuni-proxy -c httpd --tail=20
```

## Usage

Once installed, systems can be connected the to proxy.

## More Info

Check the product documentation: https://www.uyuni-project.org/uyuni-docs
