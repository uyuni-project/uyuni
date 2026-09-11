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

SCC organization mirroring credentials can be automatically set if the name of secret of `kubernetes.io/basic-auth` type with those credentials is set as `server.sccSecret` value.

The following TLS secrets are expected:

- `db-cert`: is the TLS certificate for the report database and needs to have the `db` and `reportdb` Subject Alternate Names as well as the FQDN exposed to the outside world
- `uyuni-cert`: is the TLS certificate for the ingress rule and needs to have the public FQDN as Subject Alternate Name.

Pulling images from a registry requiring authentication requires a secret of `kubernetes.io/dockerconfigjson` type.
Its name needs to be passed as the `registrySecret` value.

### ConfigMaps

The Root CA certificate of `db-cert` and `uyuni-cert` are expected in ConfigMaps named `db-ca` and `uyuni-ca` with the certificate in the `ca.crt` key.

### Persistent Volumes

The following persistent volume claims will be created and will need to be bound to persistent volumes.

| Volume Key | Default Size |
| ---------- | ------------ |
| `var-pgsql` | `"50Gi"` |
| `var-spacewalk` | `"100Gi"` |
| `var-cache` | `"10Gi"` |
| `var-log` | `"2Gi"` |
| `srv-www` | `"100Gi"` |
| `srv-tftpboot` | `"300Mi"` |
| `ca-certs` | `"10Mi"` |
| `etc-apache2` | `"1Mi"` |
| `etc-cobbler` | `"1Mi"` |
| `etc-postfix` | `"1Mi"` |
| `etc-rhn` | `"1Mi"` |
| `etc-salt` | `"1Mi"` |
| `etc-sssd` | `"1Mi"` |
| `etc-sysconfig` | `"20Mi"` |
| `etc-systemd-multi` | `"1Mi"` |
| `etc-systemd-sockets` | `"1Mi"` |
| `etc-tomcat` | `"1Mi"` |
| `run-salt-master` | `"10Mi"` |
| `srv-formulametadata` | `"10Mi"` |
| `srv-pillar` | `"10Mi"` |
| `srv-salt` | `"10Mi"` |
| `srv-spacewalk` | `"10Mi"` |
| `srv-susemanager` | `"1Mi"` |
| `var-cobbler` | `"10Mi"` |
| `var-salt` | `"10Mi"` |
| `var-search` | `"10Gi"` |

They all are using the `ReadWriteOnce` access mode and can be configured in the values.

Changing the default volume sizes according to the distributions you plan to synchronize and manage is recommended.
See the [requirements documentation](https://www.uyuni-project.org/uyuni-docs/en/uyuni/installation-and-upgrade/uyuni-install-requirements.html) for more information.

The `volumes.storageClass` can be used to change the storage class of all the persistent volume claims.
This would be overridden by each claim's `volumes.<claim>.storageClass` value.

The storage class supports a special `"-"` value to force the storage class to the empty string.
This is different from the `""` value representing the default storage class by not setting any storage class at all.

Each of the volume claims can have the `extraLabels`, `annotations` properties to add metadata to the claims.
They can also have the `volumeName` and `selector` properties to precisely associate the claim to a volume.

Additional volumes can be attached to the server pod with `server.extraVolumes` and mounted into the server container with `server.extraVolumeMounts`.
This is useful for optional content volumes like `inter-server-sync` data without changing the chart templates.

For example:

```yaml
server:
  extraVolumes:
    - name: inter-server-sync-data
      persistentVolumeClaim:
        claimName: inter-server-sync-data

  extraVolumeMounts:
    - name: inter-server-sync-data
      mountPath: /inter-server-sync-data
      readOnly: true
```

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
| TCP      | 8001  | taskomatic   | 8001         | Only if installed with `exposeJavaDebug = true`  |
| TCP      | 8002  | search       | 8002         | Only if installed with `exposeJavaDebug = true`  |
| TCP      | 8003  | tomcat       | 8003         | Only if installed with `exposeJavaDebug = true`  |

#### Exposing TFTP

Exposing the `tftp` service has to be done differently due to the way TFTP protocol is working.
Either use the host network using the `tftp.hostNetwork` value or configure a load balancer for the `tftp` service.
Note that not all load balancers will work: `serviceLB` implementation is not compatible with TFTP protocol, while MetalLB works.

#### Exposing the report database

Exposing the report database externally requires configuring the `reportdb` service as a `NodePort` or `LoadBalancer` (via the `services.reportdb` value).

Be aware of a critical security behavior: external database connections require SSL, whereas internal cluster connections do not.
However, Traefik ingress TCP routes mask client IPs, causing PostgreSQL to view external traffic as internal.

If you expose the database using a Traefik ingress TCP route and endpoint, you risk allowing clients to establish unencrypted, insecure connections.

The IP range to use for the plain text connections on the database can be configured using the `db.podsCIDR` value.
Also consider using a `NetworkPolicy` to limit the access to the database to the application namespace and the known external IPs.

#### Services values

Services can be configured with the following values:

| Key | Type | Default | Description |
| --- | ---- | ------- | ----------- |
| `type` | string | `"ClusterIP"` | type of all the externally-facing services. Can be overridden on each service. |
| `annotations` | string | `nil` | annotations to set to all the services. Merged with the service ones. |

Each of the services can be configured with an overriding `type` and `annotations`.
The list of services supporting this is:

| Service | Comment |
| `reportdb` |  |
| `salt` |  |
| `search` |  |
| `taskomatic` |  |
| `tomcat` |  |
| `tftp` | Cannot override the `type` |

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
Otherwise set the `server.selinuxType` to the name of a custom SELinux type granting only the permissions needed by the server pod.
Defining a dedicated type — rather than extending the generic `container_t` — ensures the extra permissions are not granted to every other container running on the node.
If using exactly the policy below, the name of the type to use will be `uyuni_container_t`.

To deploy the policy, create a `/root/systemdcontainerpolicy.te` file with this content:

```sepolicy
module systemdcontainerpolicy 1.0;

require {
    attribute domain;
    attribute container_domain;
    attribute mcs_constrained_type;
    attribute container_net_domain;
    attribute svirt_sandbox_domain;
    attribute sandbox_net_domain;
    attribute syslog_client_type;
    attribute can_dump_kernel;
    attribute can_receive_kernel_messages;
    attribute corenet_unconfined_type;
    attribute corenet_unlabeled_type;
    attribute kernel_system_state_reader;
    attribute pcmcia_typeattr_1;
    attribute process_user_target;
    role system_r;
    type cgroup_t;
    type tmpfs_t;
    type proc_t;
   
    class dir { search write add_name create remove_name rmdir setattr getattr mounton };
    class file { create open write append read unlink setattr getattr watch };
    class filesystem { mount getattr relabelfrom relabelto remount unmount };
    class netlink_audit_socket { nlmsg_relay };
}

# Declare the uyuni_container_t type and associate it with standard container attributes.
type uyuni_container_t, domain, container_domain, container_net_domain, mcs_constrained_type, svirt_sandbox_domain, sandbox_net_domain, syslog_client_type, can_dump_kernel, can_receive_kernel_messages, corenet_unconfined_type, corenet_unlabeled_type, kernel_system_state_reader, pcmcia_typeattr_1, process_user_target;

# Associate with the system_r role
role system_r types uyuni_container_t;

#============= uyuni_container_t ==============
allow uyuni_container_t cgroup_t:dir { add_name create remove_name rmdir setattr write search getattr };
allow uyuni_container_t cgroup_t:file { create open write append read setattr getattr unlink watch };
allow uyuni_container_t cgroup_t:filesystem { mount getattr relabelfrom relabelto };

# Allow systemd's credential helper (sd-mkdcreds) to use /dev/shm as a mount point for service credentials.
allow uyuni_container_t tmpfs_t:dir mounton;

# Standard lookups and attributes for the mount point
allow uyuni_container_t tmpfs_t:dir { getattr search };

# Allow systemd to mount/remount the proc filesystem for namespacing
allow uyuni_container_t proc_t:filesystem { mount remount unmount };

# Required to use directories as mount points
allow uyuni_container_t proc_t:dir mounton;

# Allow su and PAM inside the container to log audits to the kernel
allow uyuni_container_t self:netlink_audit_socket { nlmsg_relay };
```

Then compile and load it:

```sh
checkmodule -M -m -o /root/systemdcontainerpolicy.mod /root/systemdcontainerpolicy.te
semodule_package -o /root/systemdcontainerpolicy.pp -m /root/systemdcontainerpolicy.mod
semodule -i /root/systemdcontainerpolicy.pp
```

## Chart values

| Key | Type | Default | Description |
| --- | ---- | ------- | ----------- |
| `repository` | string | `"registry.opensuse.org/uyuni"` | The default repository with the path where to find all the images. |
| `tag` | string | `nil` | The default tag to use for all the images, can be overridden By default the tag will be the appVersion of the Chart. |
| `pullPolicy` | string | `"IfNotPresent"` | Images pull policy. See: https://kubernetes.io/docs/concepts/containers/images/#image-pull-policy |
| `registrySecret` | string | `""` | name of secret to use to pull the images from the registry with authentication. Leave empty for no authentication. To create the secret, see: https://kubernetes.io/docs/tasks/configure-pod-container/pull-image-private-registry/ |
| `global` | object | `{"fqdn":null}` | Variables that can be shared with a parent chart |
| `global.fqdn` | string | `nil` | Fully qualified name the server will answer as. |
| `timezone` | string | `"Etc/UTC"` | The time zone to set in the containers |
| `placement` | object | `{}` | Default node placement rules for all pods |
| `server` | object | `{"affinity":{},"apparmorProfile":"","email":"admin@uyuni.lab.org","extraVolumeMounts":[],"extraVolumes":[],"image":null,"mirror":{"claimName":"","hostPath":""},"nodeName":null,"nodeSelector":{},"sccSecret":"","selinuxType":"","superPrivileged":false,"systemdLogLevel":"","tag":null,"tolerations":[]}` | Server component configuration |
| `server.image` | string | `nil` | Overrides the default image computed using the repository property. Leave undefined to use the default |
| `server.tag` | string | `nil` | Overrides the default tag in the tag property. Leave undefined to use the default |
| `server.email` | string | `"admin@uyuni.lab.org"` | Email used for the notifications sent by the server. |
| `server.sccSecret` | string | `""` | Name of a basic-auth secret with the organization mirroring credentials to set up on the server. |
| `server.mirror` | object | `{"claimName":"","hostPath":""}` | Volume or host path to mount in the container as server.susemanager.fromdir value. |
| `server.mirror.claimName` | string | `""` | Name of the PVC to use for the mirror volume. |
| `server.mirror.hostPath` | string | `""` | Path on the node to mount as mirror. |
| `server.extraVolumes` | list | `[]` | Additional Kubernetes volumes for the server pod. |
| `server.extraVolumeMounts` | list | `[]` | Additional mounts for the server container. |
| `server.superPrivileged` | bool | `false` | Set to true to run on a cluster with selinux or AppArmor enforced. |
| `server.systemdLogLevel` | string | `""` | Systemd log level to use for debugging |
| `server.apparmorProfile` | string | `""` | Name of an AppArmor profile to use for the server pod. |
| `server.selinuxType` | string | `""` | Name of the SELinux type to use for the server pod. |
| `server.nodeSelector` | object | `{}` | Node label matching rules. |
| `server.affinity` | object | `{}` | Advanced scheduling rules. |
| `server.tolerations` | list | `[]` | Node taint tolerations. |
| `server.nodeName` | string | `nil` | Specific node assignment. |
| `exposeJavaDebug` | bool | `false` | Expose ports 8001, 8002, and 8003 for Java debugging if true |
| `enableMonitoring` | bool | `true` | Create the ingress or gateway routes for Prometheus exporters if true. |
| `ingress` | object | `{"annotations":{"exporters":{},"hub":{},"nossl":{},"saline":{},"ssl":{},"sslRedirect":{}},"class":"traefik","type":"traefik"}` | Configuration of the ingress rules. Won't be used if gateway is enabled. |
| `ingress.type` | string | `"traefik"` | Ingress that is used in the cluster. Set to the empty string to use a custom ingress. |
| `ingress.class` | string | `"traefik"` | Specify the ingress class name to use. |
| `ingress.annotations` | object | `{"exporters":{},"hub":{},"nossl":{},"saline":{},"ssl":{},"sslRedirect":{}}` | Custom annotations for ingress rules. |
| `ingress.annotations.ssl` | object | `{}` | annotations for the uyuni-ingress-ssl ingress definition. |
| `ingress.annotations.sslRedirect` | object | `{}` | annotations for the uyuni-ingress-ssl-redirect ingress definition. |
| `ingress.annotations.nossl` | object | `{}` | annotations for the uyuni-ingress-nossl ingress definition. |
| `ingress.annotations.saline` | object | `{}` | annotations for the saline-ingress-ssl ingress definition. |
| `ingress.annotations.hub` | object | `{}` | annotations for the hubapi-ingress-ssl ingress definition. |
| `ingress.annotations.exporters` | object | `{}` | annotations for the exporters-ingress-ssl ingress definition. |
| `gateway` | object | `{"class":"","enable":false,"listeners":{"http":{"name":"web","port":8000},"https":{"name":"websecure","port":8443}},"name":""}` | Configures the Gateway API. |
| `gateway.enable` | bool | `false` | If enabled, Gateway API 1.4 resources will be deployed instead of the ingress ones. Note that TCPRoute is used and is still in alpha2 stage. |
| `gateway.class` | string | `""` | Name of the gateway class to use. For rke2 with traefik, it is likely to be "traefik". |
| `gateway.name` | string | `""` | Name of the Gateway to use. If left empty an uyuni-gateway will be created. |
| `gateway.listeners` | object | `{"http":{"name":"web","port":8000},"https":{"name":"websecure","port":8443}}` | Gateway listeners configuration |
| `gateway.listeners.http` | object | `{"name":"web","port":8000}` | Configuration of the HTTP listener. The default is adjusted for traefik on rke2 |
| `gateway.listeners.http.name` | string | `"web"` | Name of the HTTP listener in the Gateway. |
| `gateway.listeners.http.port` | int | `8000` | Port of the HTTP listener to set in the Gateway. |
| `gateway.listeners.https` | object | `{"name":"websecure","port":8443}` | Configuration of the HTTPS listener. The default is adjusted for traefik on rke2 |
| `gateway.listeners.https.name` | string | `"websecure"` | Name of the HTTPS listener in the Gateway. |
| `gateway.listeners.https.port` | int | `8443` | Port of the HTTPS listener to set in the Gateway. |
| `tftp` | object | `{"affinity":{},"enable":false,"hostNetwork":false,"image":null,"nodeName":null,"nodeSelector":{},"tag":null,"tolerations":[]}` | TFTP server configuration |
| `tftp.image` | string | `nil` | Image for the TFTP server container, overwriting the one computed with the `repository` value |
| `tftp.tag` | string | `nil` | Tag for the TFTP server container, overwriting the global one |
| `tftp.enable` | bool | `false` | Define a TFTP server deployment with one replica if set to true |
| `tftp.hostNetwork` | bool | `false` | Use the host network to bypass the Kubernetes network layers. This may be needed if not using a LoadBalancer for the TFTP server. Note that this may be more complex to manage on multi-node clusters. |
| `tftp.nodeSelector` | object | `{}` | Node label matching rules. |
| `tftp.affinity` | object | `{}` | Advanced scheduling rules. |
| `tftp.tolerations` | list | `[]` | Node taint tolerations. |
| `tftp.nodeName` | string | `nil` | Specific node assignment. |
| `hubAPI` | object | `{"affinity":{},"enable":false,"image":null,"nodeName":null,"nodeSelector":{},"tag":null,"tolerations":[]}` | hub API component configuration |
| `hubAPI.image` | string | `nil` | Image for the hub API container, overwriting the one computed with the `repository` value |
| `hubAPI.tag` | string | `nil` | Image tag for the hub API container, overwriting the global one |
| `hubAPI.enable` | bool | `false` | Define a hub API deployment with one replica if set to true. |
| `hubAPI.nodeSelector` | object | `{}` | Node label matching rules. |
| `hubAPI.affinity` | object | `{}` | Advanced scheduling rules. |
| `hubAPI.tolerations` | list | `[]` | Node taint tolerations. |
| `hubAPI.nodeName` | string | `nil` | Specific node assignment. |
| `coco` | object | `{"affinity":{},"image":null,"nodeName":null,"nodeSelector":{},"replicas":0,"tag":null,"tolerations":[]}` | Confidential computing attestation component configuration |
| `coco.image` | string | `nil` | Image for the confidential computing containers, overwriting the one computed with the `repository` value |
| `coco.tag` | string | `nil` | Image tag for the confidential computing containers, overwriting the global one |
| `coco.replicas` | int | `0` | Number the confidential computing pods replicas |
| `coco.nodeSelector` | object | `{}` | Node label matching rules. |
| `coco.affinity` | object | `{}` | Advanced scheduling rules. |
| `coco.tolerations` | list | `[]` | Node taint tolerations. |
| `coco.nodeName` | string | `nil` | Specific node assignment. |
| `saline` | object | `{"affinity":{},"enable":false,"image":null,"nodeName":null,"nodeSelector":{},"tag":null,"tolerations":[]}` | Saline component configuration |
| `saline.image` | string | `nil` | Image for the Saline container, overwriting the one computed with the `repository` value |
| `saline.tag` | string | `nil` | Image tag for the Saline container, overwriting the global one |
| `saline.enable` | bool | `false` | Define a saline deployment with one replica if set to true. |
| `saline.nodeSelector` | object | `{}` | Node label matching rules. |
| `saline.affinity` | object | `{}` | Advanced scheduling rules. |
| `saline.tolerations` | list | `[]` | Node taint tolerations. |
| `saline.nodeName` | string | `nil` | Specific node assignment. |
| `db` | object | `{"affinity":{},"enable":true,"image":null,"internal":{"host":null,"name":null,"port":5432},"nodeName":null,"nodeSelector":{},"podsCIDR":"10.42.0.0/16","report":{"host":null,"name":null,"port":5432},"tag":null,"tolerations":[]}` | PostgreSQL database configuration |
| `db.image` | string | `nil` | Image for the database container, overwriting the one computed with the `repository` value |
| `db.tag` | string | `nil` | Image tag for the database container, overwriting the global one |
| `db.enable` | bool | `true` | Deploys the internal dababase if set to true. Setting to false is not supported yet. |
| `db.podsCIDR` | string | `"10.42.0.0/16"` | Network IP range to allow plain text connections from, usually the cluster pods CIDR The default has been adjusted with rke2's pods CIDR default. |
| `db.internal` | object | `{"host":null,"name":null,"port":5432}` | Configuration of the internal database connection |
| `db.internal.host` | string | `nil` | FQDN or IP where to find the internal database (unused for now) |
| `db.internal.port` | int | `5432` | Port where to find the internal database (unused for now) |
| `db.internal.name` | string | `nil` | Name of the internal database (unused for now) |
| `db.report` | object | `{"host":null,"name":null,"port":5432}` | Configuration of the report database connection |
| `db.report.host` | string | `nil` | FQDN or IP where to find the report database (unused for now) |
| `db.report.port` | int | `5432` | Port where to find the report database (unused for now) |
| `db.report.name` | string | `nil` | Name of the report database (unused for now) |
| `db.nodeSelector` | object | `{}` | Node label matching rules. |
| `db.affinity` | object | `{}` | Advanced scheduling rules. |
| `db.tolerations` | list | `[]` | Node taint tolerations. |
| `db.nodeName` | string | `nil` | Specific node assignment. |

## Usage

Once installed, the web interface can be accessed directly on the configured FQDN.

## More Info

Check the product documentation: https://www.uyuni-project.org/uyuni-docs
