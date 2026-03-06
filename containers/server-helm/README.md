# server-helm

This chart installs [Uyuni server](https://uyuni-project.org).
This is deploying the core features, [the uyuni-charts repository](https://github.com/uyuni-project/uyuni-charts) contains some examples Helm charts using it in conjunction with other tools.

## Configuration

### Secrets

The following secrets with `kubernetes.io/basic-auth` type are required.
They need to contain a `username` and a `password` key.

- `db-admin-credentials`: PostgreSQL administrator credentials
- `db-credentials`: credentials for the internal database user
- `reportdb-credentials`: credentials for the report database user
- `admin-credentials`: credentials for the server administrator

The following TLS secrets are expected:

- `db-cert`: is the TLS certificate for the report database and needs to have the `db` and `reportdb` Subject Alternate Names as well as the FQDN exposed to the outside world
- `uyuni-cert`: is the TLS certificate for the ingress rule and needs to have the public FQDN as Subject Alternate Name.

### ConfigMaps

The Root CA certificate of `db-cert` and `uyuni-cert` are expected in ConfigMaps named `db-ca` and `uyuni-ca` with the certificate in the `ca.crt` key.

### Persistent Volumes

The following persistent volume claims will be created and will need to be bound to persistent volumes.

- `ca-certs`: (default size: 10Mi)
- `etc-apache2`: (default size: 1Mi)
- `etc-cobbler`: (default size: 1Mi)
- `etc-postfix`: (default size: 1Mi)
- `etc-rhn`: (default size: 1Mi)
- `etc-salt`: (default size: 1Mi)
- `etc-sssd`: (default size: 1Mi)
- `etc-sysconfig`: (default size: 20Mi)
- `etc-systemd-multi`: (default size: 1Mi)
- `etc-systemd-sockets`: (default size: 1Mi)
- `etc-tomcat`: (default size: 1Mi)
- `root`: (default size: 1Mi)
- `run-salt-master`: (default size: 10Mi)
- `srv-formulametadata`: (default size: 10Mi)
- `srv-pillar`: (default size: 10Mi)
- `srv-salt`: (default size: 10Mi)
- `srv-spacewalk`: (default size: 10Mi)
- `srv-susemanager`: (default size: 1Mi)
- `srv-tftpboot`: (default size: 300Mi)
- `srv-www`: (default size: 100Gi)
- `var-cache`: (default size: 10Gi)
- `var-cobbler`: (default size: 10Mi)
- `var-log`: (default size: 2Gi)
- `var-pgsql`: (default size: 50Gi)
- `var-salt`: (default size: 10Mi)
- `var-search`: (default size: 10Gi)
- `var-spacewalk`: (default size: 100Gi)

They all are using the `ReadWriteOnce` access mode and can be configured in the values.

Changing the default volume sizes according to the distributions you plan to synchronize and manage is recommended.
See the [requirements documentation](https://www.uyuni-project.org/uyuni-docs/en/uyuni/installation-and-upgrade/uyuni-install-requirements.html) for more information.

### Exposing ports

Uyuni requires some TCP and UDP ports to be routed to its services.
Here is a list of the ports to map:


| Protocol | Port  | Service name | Service port |                                                  |
| -------- | ----- | ------------ | ------------ | ------------------------------------------------ |
| TCP      | 5432  | reportdb     | 5432         |                                                  |
| TCP      | 4505  | salt         | 4505         |                                                  |
| TCP      | 4506  | salt         | 4506         |                                                  |
| TCP      | 25151 | cobbler      | 25151        |                                                  |
| TCP      | 9100  | tomcat       | 9100         |                                                  |
| TCP      | 9187  | db           | 9187         | Not if installed with `enableMonitoring = false` |
| TCP      | 8001  | taskomatic   | 8001         | Only if installed with `exposeJavaDebug = true`  |
| TCP      | 8002  | search       | 8002         | Only if installed with `exposeJavaDebug = true`  |
| TCP      | 8003  | tomcat       | 8003         | Only if installed with `exposeJavaDebug = true`  |


Exposing the `tftp` service has to be done differently due to the way TFTP protocol is working.
Either use the host network using the `tftp.hostnetwork` value or configure a load balancer for the `tftp` service.
Note that not all load balancers will work: `serviceLB` implementation is not compatible with TFTP protocol, while MetalLB works.

### Ingress vs Gateway API

The helm chart deploys ingress rules by default.
Switching to [Gateway API](https://gateway-api.sigs.k8s.io/) instead is possible though requires more effort.
The Gateway API implementation used in this helm chart is aligned with the one handled by the traefik shipped with the latest RKE2.

Using the Gateway API routes is still experimental as some of the needed resources, namely `TCPRoute` are not stable yet.
To enable it, set the `gateway.enable` value.
The other values in the `gateway` structure may need to be set depending on the cluster setup.

**Note that on RKE2 1.35 on top of enabling Traefik with Gateway API, the `TLSRoute` and `TCPRoute` CRDs need to be manually added and the Traefik helm chart has to be deployed with the `providers.kubernetesGateway.experimentalChannel`.**

## Usage

Once installed, the web interface can be accessed directly on the configured FQDN.

## More Info

Check the product documentation: https://www.uyuni-project.org/uyuni-docs
