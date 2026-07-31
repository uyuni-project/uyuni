# kubectl mlm-supportconfig

A `kubectl` plugin that collects a complete support bundle for SUSE
Multi-Linux Manager workloads running on Kubernetes — works against
any pod deployed by `server-helm` or `proxy-helm`.

Two modes:

```bash
kubectl mlm-supportconfig <namespace>            # all pods in the namespace
kubectl mlm-supportconfig <pod> <namespace>      # just that one pod
```

For each selected pod the plugin collects, best-effort:

* `supportconfig` in every container of the pod that has the binary,
  invoked with `-t <dir>`. Multi-container pods produce
  one `supportconfig-<container>/` directory per container that has
  `supportutils` installed; containers without it are silently skipped;
* `kubectl logs --all-containers` with timestamps and per-container
  prefixes, both current and previous container instances;
* `kubectl describe pod` and `kubectl get pod -o yaml`.

Once per distinct node hosting any selected pod, it also runs the
[Rancher logs-collector](https://github.com/rancherlabs/support-tools/blob/master/collection/rancher/v2.x/logs-collector/README.md)
via `kubectl debug node/...` — this captures node-level data (journald,
container runtime logs, kernel info, network config) that no pod-side
collection can see.

It also dumps, for the target namespace:

* namespace events (sorted by `lastTimestamp`, text and YAML);
* namespace objects beyond pods — Deployments, Services, Endpoints,
  EndpointSlices, NetworkPolicies, ConfigMaps, Ingress,
  Traefik `IngressRoute`/`IngressRouteTCP`, PVCs, cert-manager
  Certificates (each best-effort: CRDs that aren't installed are
  silently skipped);
* the list of secrets in the namespace and their type;
* a resource-usage snapshot — `kubectl top pod --containers` and
  `kubectl top node` (silently skipped if `metrics-server` isn't
  installed in the cluster);
* helm release state — `helm list` plus per-release `helm status`,
  `helm history`, and `helm get notes` (skipped entirely if `helm` is
  not on the workstation's `$PATH`). The plugin intentionally does
  **not** dump `helm get values` or `helm get manifest`: both commonly
  contain install-time passwords (db, cert, admin) and rendered
  `Secret` objects with base64-encoded data.

Once per run, it also captures cluster baseline — `kubectl version`,
`kubectl get nodes -o wide`, the full node spec, and cluster-scoped
storage (`PersistentVolumes`, `StorageClasses`) so the bundle is
self-contained for the reviewer.

Everything ends up in a single tarball in the current directory, with
a top-level `SUMMARY.txt` describing what was collected and what was
skipped. The plugin never aborts on a collection failure — missing
permissions, missing binaries, timeouts, and `kubectl cp` failures are
all recorded in `SUMMARY.txt` and the next step runs. Exit code is `0`
whenever a bundle was produced.

## Prerequisites

On the workstation:

* `kubectl` on `$PATH`, configured for the target cluster.
* `bash`

In the target cluster, the caller's kubeconfig should have:

* `get` / `list` on `pods` and read access to the target namespace;
* `get` / `list` on `events` in the target namespace;
* `create` / `delete` on `pods` and access to `nodes/proxy`
  (`kubectl debug node/...`);
* `create` on `pods/exec` in the target namespace;
* the cluster should allow privileged pods on the target nodes for the
  Rancher step to succeed (default on K3s/RKE2; on restricted clusters
  a `PodSecurity` policy may block it — that step then records
  `SKIPPED` and the rest of the bundle is still produced).

Any missing permission only affects the steps that need it.

## Installation

A `kubectl` plugin is just an executable on `$PATH` whose name starts
with `kubectl-`. The underscore in the filename becomes a hyphen in
the invocation.

Download the plugin from the upstream repository and install it:

```bash
# Download
curl -sSLO https://raw.githubusercontent.com/uyuni-project/uyuni/refs/heads/master/containers/mlm-supportconfig/kubectl-mlm_supportconfig

# Check the file
curl -sSL https://raw.githubusercontent.com/uyuni-project/uyuni/refs/heads/master/containers/mlm-supportconfig/kubectl-mlm_supportconfig.sha512 | sha512sum -c

# system-wide
sudo install -m 0755 kubectl-mlm_supportconfig /usr/local/bin/

# or per-user, assuming ~/bin is on your PATH
install -m 0755 kubectl-mlm_supportconfig ~/bin/

# verify
kubectl plugin list | grep mlm-supportconfig
```

## Usage

```bash
kubectl mlm-supportconfig <namespace>            # all pods in the namespace
kubectl mlm-supportconfig <pod> <namespace>      # one specific pod
```

The first argument that you'd otherwise need to look up:

```bash
kubectl get pods -n <namespace>
```

A bundle lands in the current directory, named:

* `mlm-supportconfig-<namespace>-<pod>-<timestamp>.tar.gz` in single-pod mode
* `mlm-supportconfig-<namespace>-all-<timestamp>.tar.gz` in whole-namespace mode

The terminal output ends with a `Summary:` block listing every step and
its outcome (`OK` / `SKIPPED` / `FAILED` / `TIMEOUT` / `PARTIAL`); the
same content is shipped as `SUMMARY.txt` inside the tarball.

### Air-gapped clusters

The per-node Rancher collector uses the `rancherlabs/swiss-army-knife`
image from Docker Hub. On a cluster without internet access, mirror
that image to your internal registry once and point the plugin at the
mirror via the `RANCHER_SUPPORT_IMAGE` env var:

```bash
# Mirror once (from a machine that can reach Docker Hub)
skopeo copy --all \
    docker://rancherlabs/swiss-army-knife:latest \
    docker://registry.internal.example.com/rancherlabs/swiss-army-knife:v1.2.3

# Then run the plugin pointing at the mirror
export RANCHER_SUPPORT_IMAGE=registry.internal.example.com/rancherlabs/swiss-army-knife:v1.2.3
kubectl mlm-supportconfig <namespace>
```

When unset, the plugin defaults to `rancherlabs/swiss-army-knife`. Pin
to an explicit tag (not `:latest`) in air-gapped use so reproducibility
isn't at the mercy of upstream updates.

If the configured image can't be pulled (no network, missing tag,
auth failure), the rancher step records
`FAILED (ImagePullBackOff)` and the rest of the bundle is still
produced — `supportconfig`, `kubectl logs`, namespace objects, and
helm state need no extra image.
