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
- `var-pgsql18`: (default size: 50Gi)
- `var-salt`: (default size: 10Mi)
- `var-search`: (default size: 10Gi)
- `var-spacewalk`: (default size: 100Gi)

They all are using the `ReadWriteOnce` access mode and can be configured in the values.

Changing the default volume sizes according to the distributions you plan to synchronize and manage is recommended.
See the [requirements documentation](https://www.uyuni-project.org/uyuni-docs/en/uyuni/installation-and-upgrade/uyuni-install-requirements.html) for more information.

### Node Tuning

For each of the components it is possible to tune the node where the pod will be scheduled.
This chart supports a **default** configuration with **local overrides**, allowing baseline rules to be set for all pods and customized for specific components when needed.

Scheduling can be controlled using `nodeSelector`, `affinity`, `tolerations`, or `nodeName`. It is not necessary to use all of them; simply choose the method that matches the cluster's scheduling strategy.

For example, to set a baseline rule for all components but override the placement for the `db` pod specifically, the `values.yaml` would look like this:

```yaml
# DEFAULTS
# These rules apply to all pods unless overridden by a specific component.
placement:
  nodeSelector:
    environment: production

  tolerations:
  - key: "server-tier"
    operator: "Equal"
    value: "true"
    effect: "NoSchedule"

# LOCAL OVERRIDES
# These rules apply ONLY to the specific component and override the global equivalents.
db:
  nodeSelector:
    "kubernetes.io/hostname": "node-42"

  affinity:
    nodeAffinity:
      requiredDuringSchedulingIgnoredDuringExecution:
        nodeSelectorTerms:
        - matchExpressions:
          - key: "kubernetes.io/hostname"
            operator: In
            values:
            - "node-42"

  # nodeName: "node-42"
```

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

### AppArmor

If the node where the server pod is running has AppArmor, the containerd profile won't let it mount the cgroup2 file system.
This can be addressed in two different ways.
The easiest, but unsafe way is to set `server.superPrivileged=true` value so the server containers run unconfined.
Otherwise set the `server.apparmorProfile` to the name of a profile containing a definition like the following.
If using exactly this content, the name of the profile to use will be `k8s-systemd-uyuni`.

To deploy the AppArmor profile, copy this content to `/etc/apparmor.d/k8s-systemd-uyuni` and run `apparmor_parser -r /etc/apparmor.d/k8s-systemd-uyuni` to load it.

```
#include <tunables/global>

profile k8s-systemd-uyuni flags=(attach_disconnected,mediate_deleted) {
  #include <abstractions/base>
  #include <abstractions/nameservice>

  # Standard container permissions
  file,
  network,
  capability,
  ptrace,
  unix,

  # Deny writes to critical kernel interfaces that systemd doesn't need to change
  deny /sys/firmware/** rwklx,
  deny /sys/kernel/debug/** rwklx,

  # Broadly allow the specific flag combinations used for systemd hardening
  # This covers /dev/pts/, /dev/mqueue/, and the previous /etc/ errors.
  mount options=(ro, nosuid, noexec, nodev, remount, bind) -> **,
  mount options=(ro, nosuid, noexec, remount, bind) -> **,
  mount options=(ro, nosuid, nodev, remount, bind) -> **,
  mount options=(ro, nosuid, remount, bind) -> **,
  mount options=(ro, remount, bind) -> **,

  # Allow mount propagation (Required for systemd to function at all)
  mount options=(rw, rslave) -> **,
  mount options=(rw, slave) -> **,
  mount options=(rw, shared) -> **,

  # Specific filesystem types for systemd's API mounts
  mount fstype=tmpfs options=(rw, nosuid, nodev, noexec) -> /tmp/,
  mount fstype=tmpfs options=(rw, nosuid, nodev) -> /tmp/,
  mount fstype=tmpfs -> /run/**,
  mount fstype=cgroup2 -> /sys/fs/cgroup/,
  mount fstype=mqueue -> /dev/mqueue/,
  mount fstype=fusectl -> /sys/fs/fuse/connections/,
  mount fstype=devpts -> /dev/pts/,

  # Generic remounts (for general compatibility)
  mount options=(rw, remount) -> **,
  mount options=(ro, remount) -> **, 
  
  # Required for the uyuni server container specifically
  /sys/fs/cgroup/** rw,
  /run/** rw,
  /var/** rw,
  # Allow reading the various config volumes mapped in the chart
  /etc/** r,
}
```

### SELinux

If the node where the server pod is running has SELinux and RKE2 is configured to use it, the container won't be able to mount the cgroup2 file system.
This can be addressed in two different ways.
The easiest, but unsafe way is to set `server.superPrivileged=true` value so the server containers run with the `spc_t` label.
Otherwise apply the following custom policy:

* Create a `/root/systemdcontainerpolicy.te` file with this content:

```sepolicy
module systemdcontainerpolicy 1.0;

require {
    type container_t;
    type cgroup_t;
    type tmpfs_t;
    type proc_t;
    
    class dir { search write add_name create remove_name rmdir setattr getattr mounton search };
    class file { create open write append read unlink setattr getattr watch };
    class filesystem { mount getattr relabelfrom relabelto };
}

#============= container_t ==============
allow container_t cgroup_t:dir { add_name create remove_name rmdir setattr write search getattr };
allow container_t cgroup_t:file { create open write append read setattr getattr unlink watch };
allow container_t cgroup_t:filesystem { mount getattr relabelfrom relabelto };

# Allow systemd's credential helper (sd-mkdcreds) to use /dev/shm as a mount point for service credentials.
allow container_t tmpfs_t:dir mounton;

# Standard lookups and attributes for the mount point
allow container_t tmpfs_t:dir { getattr search };

# Allow systemd to mount/remount the proc filesystem for namespacing
allow container_t proc_t:filesystem { mount remount unmount };

# Required to use directories as mount points
allow container_t proc_t:dir mounton;
```

* Apply it:

```sh
checkmodule -M -m -o /root/systemdcontainerpolicy.mod /root/systemdcontainerpolicy.te
semodule_package -o /root/systemdcontainerpolicy.pp -m /root/systemdcontainerpolicy.mod
semodule -i /root/systemdcontainerpolicy.pp
```

## Usage

Once installed, the web interface can be accessed directly on the configured FQDN.

## More Info

Check the product documentation: https://www.uyuni-project.org/uyuni-docs
