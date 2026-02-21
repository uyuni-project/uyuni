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
