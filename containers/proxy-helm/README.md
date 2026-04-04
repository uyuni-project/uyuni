# proxy-helm

This chart installs [Uyuni proxy](https://uyuni-project.org).
This is deploying the core features, [the uyuni-charts repository](https://github.com/uyuni-project/uyuni-charts) contains some examples Helm charts using it in conjunction with other tools.

## Configuration

### Secrets

The following TLS secrets are expected:

- `proxy-cert`: is the TLS certificate for the ingress rule and needs to have the public FQDN as Subject Alternate Name.

### ConfigMaps

The Root CA certificate of `proxy-cert` is expected in a ConfigMap `uyuni-ca` with the certificate in the `ca.crt` key.

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
Either use the host network using the `tftp.hostnetwork` value or configure a load balancer for the `tftp` service.
Note that not all load balancers will work: `serviceLB` implementation is not compatible with TFTP protocol, while MetalLB works.

## Usage

Once installed, systems can be connected the to proxy.

## More Info

Check the product documentation: https://www.uyuni-project.org/uyuni-docs
