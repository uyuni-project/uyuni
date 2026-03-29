# Uyuni Local Setup Guide (RKE2 + Helm + Tiny Test Client)

This document is a practical, contributor-focused guide to build a **local Uyuni lab** on one Linux machine.
It includes full explanations for each step so you understand not just what to run, but why.

The target outcome is:

- Uyuni running on Kubernetes (RKE2)
- Web UI reachable from your host machine (including macOS host when using VS Code SSH)
- one lightweight test client (`micro-client`) registered to Uyuni
- minimal channels synced
- package metadata refresh working end-to-end

---

## 0. High-Level Architecture (What You Are Building)

You are building a small lab with these components:

1. **RKE2 Kubernetes cluster** on your Linux machine.
2. **Uyuni server** deployed via Helm (`server-selfsigned` chart).
3. **In-cluster test minion** (`micro-client`) for quick functional checks.
4. **Channel sync** to provide packages/metadata to managed clients.
5. **Host mapping (FQDN -> ingress IP)** so clients can reach Uyuni over HTTPS.

Why this matters:

- If Uyuni’s hostname is only internal Kubernetes DNS (for example `web.uyuni.svc.cluster.local`), external clients cannot fetch repo metadata.
- A reachable FQDN (for example `uyuni.home.arpa`) keeps bootstrap and package URLs valid.

---

## 1. Prerequisites

### Required

- Linux machine with sudo access
- RKE2 installed and running
- `helm` installed
- Git checkout of:
  - `uyuni` repository
  - `uyuni-charts` repository as sibling path so `uyuni/uyuni-charts` symlink resolves

### Recommended resources for smoother setup

- 8 vCPU
- 16 GB RAM
- stable internet (repo sync can be bandwidth-heavy)

### Verify baseline quickly

```bash
which kubectl
which helm
sudo systemctl status rke2-server --no-pager
```

---

## 2. kubectl and KUBECONFIG (Most Common Early Failure Point)

### Why this step is needed

RKE2 ships its own `kubectl` binary at:

```text
/var/lib/rancher/rke2/bin/kubectl
```

That path is usually not in your shell `PATH`, so `kubectl` may not be found or you may run another kubectl build.

Also, if `KUBECONFIG` is not set, `kubectl`/`helm` often default to `http://localhost:8080`, causing:

```text
The connection to the server localhost:8080 was refused
```

### Fix

```bash
sudo ln -sf /var/lib/rancher/rke2/bin/kubectl /usr/local/bin/kubectl
echo 'export KUBECONFIG=/etc/rancher/rke2/rke2.yaml' >> ~/.zshrc
export KUBECONFIG=/etc/rancher/rke2/rke2.yaml
```

### Verify

```bash
kubectl get nodes
kubectl cluster-info
```

If these work, your CLI is pointing to RKE2 correctly.

---

## 3. Helm Dependencies for `server-selfsigned`

Move into chart directory:

```bash
cd /home/<user>/Desktop/githubrepo/uyuni/uyuni-charts/server-selfsigned
```

Add required repos:

```bash
helm repo add rancher-latest https://releases.rancher.com/server-charts/latest
helm repo add jetstack https://charts.jetstack.io
helm repo update
helm dependency update
```

### Why `jetstack` is needed

`jetstack` is the chart publisher behind cert-manager charts. Uyuni chart dependencies may reference charts from this repo.

### If dependency update fails with OCI "not found"

Example failure:

```text
.../server-helm:2026.1.0: not found
```

Meaning:

- your chart metadata references a version not available in OCI registry yet.

What to do:

- switch to compatible branch/tag where `Chart.lock` matches published artifacts
- or use branch indicated by your mentors/project issue

---

## 4. Create a Lab Values File (`values-lab.yaml`)

From repo root (`/home/<user>/Desktop/githubrepo/uyuni`), create:

```yaml
credentials:
  admin:
    user: admin
    password: "ChangeMeAdmin123!"
  db:
    internal:
      user: susemanager
      password: "ChangeMeDB1!"
    admin:
      user: postgres
      password: "ChangeMeDB2!"
    reportdb:
      user: pythia
      password: "ChangeMeDB3!"

global:
  fqdn: "uyuni.home.arpa"

server-helm:
  repository: "registry.opensuse.org/systemsmanagement/uyuni/master/containerfile/uyuni"
  tag: "2026.03"
  server:
    superPrivileged: true
  ingress:
    type: ""
    class: nginx
```

### Why these keys matter

- `global.fqdn` is the canonical hostname used in generated URLs (bootstrap, repos, API endpoints).
- `server-helm.repository` and `server-helm.tag` must match the chart generation line.
  For this chart version, using `registry.opensuse.org/uyuni/server:latest` can crash at startup with:
  `.../00-kubernetes-systemd: no such file or directory`.
- `superPrivileged: true` is commonly needed for containerized server behavior in local/lab setups.
- credentials are bootstrap admin/DB credentials used by chart initialization.

---

## 5. Deploy Uyuni

### Before Helm install: make sure cluster DNS can resolve your `global.fqdn`

This is required for the setup job.

If `global.fqdn` resolves only on your host via `/etc/hosts`, setup can fail because pods do not use your host `/etc/hosts`.

Typical failure symptom:

- `uyuni` pod stuck at `Init:1/2`
- `uyuni-setup-*` job in `Error`
- setup logs contain:
  - `Neither IPv4 nor IPv6 addresses can be resolved for configured FQDN: uyuni.home.arpa`

Check resolution from inside cluster DNS:

```bash
export KUBECONFIG=/etc/rancher/rke2/rke2.yaml
kubectl -n default run dnscheck --image=busybox:1.36 --restart=Never --command -- sh -lc 'nslookup uyuni.home.arpa || true'
kubectl -n default logs dnscheck
kubectl -n default delete pod dnscheck --force --grace-period=0
```

If it does not resolve and you do not have internal DNS yet, add a temporary CoreDNS mapping (RKE2 default CoreDNS configmap):

```bash
kubectl -n kube-system edit configmap rke2-coredns-rke2-coredns
```

In `Corefile`, inside `.:53 { ... }`, add:

```text
hosts {
    192.168.116.69 uyuni.home.arpa
    fallthrough
}
```

Then restart CoreDNS:

```bash
kubectl -n kube-system rollout restart deploy/rke2-coredns-rke2-coredns
kubectl -n kube-system rollout status deploy/rke2-coredns-rke2-coredns
```

How to get the real values:

1. Pick your FQDN (example: `uyuni.home.arpa`).
   Use any name you control and can map in DNS/hosts.
2. Get the node/ingress IP:

```bash
export KUBECONFIG=/etc/rancher/rke2/rke2.yaml
kubectl get nodes -o wide
kubectl -n uyuni get ingress -o wide
```

For single-node local labs, use the node `INTERNAL-IP` as ingress IP if ingress `ADDRESS` is empty.

3. Set variables so you can reuse them in commands:

```bash
export UYUNI_IP=192.168.116.69
export UYUNI_FQDN=uyuni.home.arpa
echo "$UYUNI_IP $UYUNI_FQDN"
```

4. Use those values in CoreDNS `hosts` block:

```text
hosts {
    <UYUNI_IP> <UYUNI_FQDN>
    fallthrough
}
```

After DNS works from pods, run Helm install:

```bash
export KUBECONFIG=/etc/rancher/rke2/rke2.yaml
cd /home/<user>/Desktop/githubrepo/uyuni

kubectl create ns uyuni
helm upgrade --install uyuni ./uyuni-charts/server-selfsigned -n uyuni -f values-lab.yaml
kubectl -n uyuni get pods -w
```

### What to wait for

You should eventually see:

- `db-*` pod `Running`
- `tftp-*` pod `Running`
- `uyuni-*` deployment pod `Running`
- `uyuni-setup-*` job pod `Completed`

### If setup job fails

```bash
kubectl -n uyuni get jobs
kubectl -n uyuni logs job/<uyuni-setup-job-name> --all-containers=true --tail=200
```

If you changed image repository/tag, FQDN, or DNS settings after a failed attempt, do a clean reset before retrying:

```bash
kubectl delete ns uyuni --wait=true
kubectl create ns uyuni
helm upgrade --install uyuni ./uyuni-charts/server-selfsigned -n uyuni -f values-lab.yaml
```

---

## 6. Make FQDN Reachable From Clients and Host Browser

This is critical.

Your `global.fqdn` is `uyuni.home.arpa`. Every client that must talk to Uyuni needs to resolve this name to the ingress IP.

### Find ingress IP

```bash
kubectl -n uyuni get ingress
```

Assume it is `192.168.116.69`.

### On Linux server (where cluster runs)

```bash
grep -q 'uyuni.home.arpa' /etc/hosts || echo "192.168.116.69 uyuni.home.arpa" | sudo tee -a /etc/hosts
```

### On macOS host (if using browser there while SSHing into Linux via VS Code)

```bash
sudo sh -c 'grep -q "uyuni.home.arpa" /etc/hosts || echo "192.168.116.69 uyuni.home.arpa" >> /etc/hosts'
sudo dscacheutil -flushcache
sudo killall -HUP mDNSResponder
```

### Verify from both machines

```bash
getent hosts uyuni.home.arpa   # Linux
```

On macOS:

```bash
dscacheutil -q host -a name uyuni.home.arpa
```

Then open:

```text
https://uyuni.home.arpa
```

### Important network note

If you change Wi-Fi/network, your local private IP can change. This breaks RKE2 (etcd),
DNS, and salt connectivity. See **Section 12: Handling IP/Network Changes** for the
full fix procedure, or just run `~/fix-uyuni-ip.sh`.

---

## 7. Create Minimal Channels (Small Practical Set)

Inside Uyuni pod:

```bash
kubectl -n uyuni exec deploy/uyuni -- \
  spacewalk-common-channels -u admin -p 'ChangeMeAdmin123!' -a x86_64 \
  opensuse_micro6_1 opensuse_micro6_1-uyuni-client
```

### Why these channels

- `opensuse_micro6_1-x86_64`: base content
- `opensuse_micro6_1-uyuni-client-x86_64`: client tools and integration content

This is a minimal workable combo for a tiny lab.

### Sync channels

```bash
kubectl -n uyuni exec deploy/uyuni -- sh -lc \
  'spacewalk-repo-sync -c opensuse_micro6_1-x86_64 && spacewalk-repo-sync -c opensuse_micro6_1-uyuni-client-x86_64'
```

### If sync says "attempting to run more than one instance"

This means another `spacewalk-repo-sync` process is already running and holding the lock.

Check active process:

```bash
kubectl -n uyuni exec deploy/uyuni -- pgrep -af '/usr/bin/spacewalk-repo-sync'
```

If a process is running, wait for it to finish.

If no process is running but the lock is stale, remove it:

```bash
kubectl -n uyuni exec deploy/uyuni -- rm -f /run/spacewalk-repo-sync.pid
```

Then rerun the sequential sync command.

### About GPG key warnings during sync

You may see warnings like:

```text
Failed to import key d832c631 ...
```

If sync still ends with `Sync completed`, continue. Validate end-to-end by minion package refresh.

### Create activation key before bootstrap (critical)

This is required for channel auto-subscription.

Why:

- bootstrap writes `activation_key` to minion config, but Uyuni must already have a matching key object
- if key is missing/invalid, system can register and ping via Salt but gets no channels

Create the key in UI:

1. `Systems` -> `Activation Keys` -> `Create Key`
2. Key label: `1-opensuse_micro6_1-x86_64`
3. Assign Base channel: `opensuse_micro6_1-x86_64`
4. Assign Child channel: `opensuse_micro6_1-uyuni-client-x86_64`
5. Save

CLI alternative (`spacecmd`):

```bash
export UYUNI_PASS='ChangeMeAdmin123!'
kubectl -n uyuni exec deploy/uyuni -- \
  spacecmd -y -u admin -p "$UYUNI_PASS" \
  activationkey_create -- \
  -n opensuse_micro6_1-x86_64 \
  -d "Leap Micro 6.1 key" \
  -b opensuse_micro6_1-x86_64

kubectl -n uyuni exec deploy/uyuni -- \
  spacecmd -y -u admin -p "$UYUNI_PASS" \
  activationkey_addchildchannels 1-opensuse_micro6_1-x86_64 opensuse_micro6_1-uyuni-client-x86_64

kubectl -n uyuni exec deploy/uyuni -- \
  spacecmd -y -u admin -p "$UYUNI_PASS" \
  activationkey_details 1-opensuse_micro6_1-x86_64
```

Important:

- with `spacecmd activationkey_create`, pass `-n opensuse_micro6_1-x86_64` (without org prefix)
- `spacecmd` prepends org id automatically
- if you pass `-n 1-opensuse...`, it creates `1-1-opensuse...` and later commands using `1-opensuse...` fail

Then use exactly the same token in bootstrap:

```bash
--activation-keys=1-opensuse_micro6_1-x86_64
```

---

## 8. Create and Register Tiny Test Client (`micro-client`)

### Create deployment (persistent)

The micro-client is a Kubernetes pod, not a full VM. Without persistent storage, every pod
restart (cluster reboot, IP change, OOM) loses the salt-minion installation and you must
re-bootstrap from scratch.

Use the deployment manifest with PersistentVolumeClaims so the salt installation survives
pod restarts:

```bash
kubectl -n uyuni delete pod micro-client --ignore-not-found=true
kubectl -n uyuni delete deploy micro-client --ignore-not-found=true
kubectl apply -f micro-client-deployment.yaml
kubectl -n uyuni rollout status deploy/micro-client --timeout=120s
```

The manifest (`micro-client-deployment.yaml` in repo root) creates three PVCs:

| PVC            | Mount path                  | Purpose                                    |
|----------------|-----------------------------|--------------------------------------------|
| `salt-config`  | `/etc/venv-salt-minion`     | minion id, keys, master config             |
| `salt-lib`     | `/usr/lib/venv-salt-minion` | Python venv with salt binaries             |
| `salt-persist` | `/opt/salt-persist`         | backup of `/usr/bin/venv-salt-minion` + machine-id |

On startup, the entrypoint script automatically:
1. Restores `/etc/machine-id` from the persistent volume
2. Restores `/usr/bin/venv-salt-minion` if it was lost (ephemeral layer)
3. Starts `venv-salt-minion -d` if a bootstrap installation exists

### Generate bootstrap script from Uyuni

```bash
kubectl -n uyuni exec deploy/uyuni -- \
  rhn-bootstrap --activation-keys=1-opensuse_micro6_1-x86_64 \
  --force-bundle --script=bootstrap-micro.sh --hostname=uyuni.home.arpa -v
```

### Ensure minion resolves Uyuni hostname

```bash
kubectl -n uyuni exec deploy/micro-client -- sh -lc \
  'grep -q "uyuni.home.arpa" /etc/hosts || echo "192.168.116.69 uyuni.home.arpa" >> /etc/hosts'
```

### Run bootstrap on minion

```bash
kubectl -n uyuni exec deploy/micro-client -- sh -lc \
  'curl -k https://uyuni.home.arpa/pub/bootstrap/bootstrap-micro.sh -o /tmp/bootstrap.sh && sh /tmp/bootstrap.sh'
```

### Container client caveat (important for this guide)

This guide uses `micro-client` as a Kubernetes pod, not a full VM.  
In this environment you can see messages like:

- `hostname: command not found`
- `System has not been booted with systemd as init system`

These are expected for minimal container images.  
Use the exact manual completion steps below:

```bash
# 1) Set fixed minion id and point minion transport to in-cluster Salt service
kubectl -n uyuni exec deploy/micro-client -- sh -lc '
  MID=$(cat /etc/machine-id 2>/dev/null || true)
  if [ -z "$MID" ]; then
    cat /proc/sys/kernel/random/uuid | tr -d "-" > /etc/machine-id
  fi
  printf "micro-client\n" > /etc/venv-salt-minion/minion_id
  sed -i "s/^master:.*/master: salt.uyuni.svc.cluster.local/" /etc/venv-salt-minion/minion.d/susemanager.conf
'

# 2) Ensure one clean minion daemon process
kubectl -n uyuni exec deploy/micro-client -- sh -lc '
  pkill -f "^/usr/lib/venv-salt-minion/bin/python.original /usr/bin/venv-salt-minion -d$" || true
  /usr/bin/venv-salt-minion -d
'

# 3) Persist salt binary and machine-id so they survive pod restarts
kubectl -n uyuni exec deploy/micro-client -- sh -c '
  cp /usr/bin/venv-salt-minion /opt/salt-persist/venv-salt-minion 2>/dev/null || true
  cp /etc/machine-id /opt/salt-persist/machine-id 2>/dev/null || true
  echo "Persisted salt binary and machine-id to /opt/salt-persist/"
'

# 4) Accept key on Uyuni and verify connectivity
kubectl -n uyuni exec deploy/uyuni -- salt-key -a micro-client -y
kubectl -n uyuni exec deploy/uyuni -- salt 'micro-client' test.ping
```

Expected:

```text
micro-client:
    True
```

If `salt-key -a micro-client -y` says key not found, wait a few seconds and list keys:

```bash
kubectl -n uyuni exec deploy/uyuni -- salt-key -L
```

then accept again.

### Verify registration (final check)

```bash
kubectl -n uyuni exec deploy/uyuni -- salt 'micro-client' test.ping
```

Expected:

```text
micro-client:
    True
```

---

## 9. Apply Channel State and Validate Package Metadata Refresh

```bash
kubectl -n uyuni exec deploy/uyuni -- salt 'micro-client' saltutil.refresh_pillar
kubectl -n uyuni exec deploy/uyuni -- salt 'micro-client' state.apply channels
kubectl -n uyuni exec deploy/micro-client -- sh -lc "grep '^baseurl=' /etc/zypp/repos.d/susemanager:channels.repo"
kubectl -n uyuni exec deploy/uyuni -- salt 'micro-client' pkg.refresh_db
```

### Success criteria

- `baseurl` entries point to `https://uyuni.home.arpa:443/...`
- `pkg.refresh_db` reports `True` for both repos

---

## 10. Tiny End-to-End Action Test (What It Means)

When we say "tiny test action", we mean proving Uyuni can send and execute management actions on the client.

Simple test:

```bash
kubectl -n uyuni exec deploy/uyuni -- salt 'micro-client' cmd.run 'cat /etc/os-release | head -n 3'
```

If this works, your control path (Uyuni -> Salt -> minion) is functioning.

---

## 11. Deep Troubleshooting (Based on Real Errors)

### Problem A: `localhost:8080 refused`

Symptom:

```text
The connection to the server localhost:8080 was refused
```

Cause:

- shell missing `KUBECONFIG`

Fix:

```bash
export KUBECONFIG=/etc/rancher/rke2/rke2.yaml
```

---

### Problem B: `uyuni` pod stuck at `Init:1/2` and setup job fails

Symptoms:

- `kubectl -n uyuni get pods` shows `uyuni-*` as `Init:1/2`
- `kubectl -n uyuni get jobs` shows `uyuni-setup-*` as `Failed` or `Error`
- setup log contains:
  - `Neither IPv4 nor IPv6 addresses can be resolved for configured FQDN`

Check:

```bash
kubectl -n uyuni get pods
kubectl -n uyuni get jobs
kubectl -n uyuni logs job/<uyuni-setup-job-name> --all-containers=true --tail=200
```

Fix:

1. Ensure your FQDN resolves from inside cluster DNS (not only from host `/etc/hosts`)
2. If needed, add CoreDNS `hosts` mapping in `rke2-coredns-rke2-coredns` configmap
3. Restart CoreDNS and re-run:

```bash
helm upgrade --install uyuni ./uyuni-charts/server-selfsigned -n uyuni -f values-lab.yaml
```

---

### Problem C: `CrashLoopBackOff` with missing `00-kubernetes-systemd`

Symptom:

- `kubectl -n uyuni get pods` shows `uyuni-*` in `CrashLoopBackOff`
- `kubectl -n uyuni describe pod <uyuni-pod>` shows:
  - `exec: "/docker-entrypoint-init.d/00-kubernetes-systemd": stat ... no such file or directory`

Cause:

- wrong server image repository/tag for this chart version.

Fix:

1. Use chart-compatible image settings in `values-lab.yaml`:

```yaml
server-helm:
  repository: "registry.opensuse.org/systemsmanagement/uyuni/master/containerfile/uyuni"
  tag: "2026.03"
```

2. Redeploy:

```bash
helm upgrade --install uyuni ./uyuni-charts/server-selfsigned -n uyuni -f values-lab.yaml
kubectl -n uyuni get pods -w
```

---

### Problem D: Setup completed but app returns 404 / not ready (`RbacRouteValidationException`)

Symptom:

- `uyuni-setup-*` is `Completed`, but `uyuni-*` is not becoming ready
- apache probes hit `/rhn/manager/api/api/getVersion` and keep getting `404`
- Tomcat log (`/var/log/tomcat/localhost.log`) shows:
  - `RbacRouteValidationException: RBAC data validation failed`

Cause:

- stale persisted state from previous failed/mismatched deployments.

Fix (clean lab reset):

```bash
export KUBECONFIG=/etc/rancher/rke2/rke2.yaml
kubectl delete ns uyuni --wait=true
kubectl create ns uyuni
helm upgrade --install uyuni ./uyuni-charts/server-selfsigned -n uyuni -f values-lab.yaml
kubectl -n uyuni get jobs,pods -w
```

---

### Problem E: Repos still point to `web.uyuni.svc.cluster.local`

Symptom:

- `pkg.refresh_db` fails
- channel repo baseurl shows `https://web.uyuni.svc.cluster.local:443/...`

Why it happens:

- Uyuni internal hostname was used at install time
- client/channel metadata still generated with old hostname

#### Step 1: Verify current server hostname config

```bash
kubectl -n uyuni exec deploy/uyuni -- sh -lc "grep '^java.hostname' /etc/rhn/rhn.conf"
```

If not your desired FQDN, continue.

#### Step 2: Ensure name resolves inside Uyuni pod

```bash
kubectl -n uyuni exec deploy/uyuni -- sh -lc \
  'grep -q "uyuni.home.arpa" /etc/hosts || echo "192.168.116.69 uyuni.home.arpa" >> /etc/hosts; getent hosts uyuni.home.arpa'
```

#### Step 3: Run hostname rename

```bash
kubectl -n uyuni exec deploy/uyuni -- sh -lc 'UYUNI_HOSTNAME=uyuni.home.arpa spacewalk-hostname-rename'
kubectl -n uyuni rollout restart deploy/uyuni
kubectl -n uyuni rollout status deploy/uyuni --timeout=10m
```

If output says `Unchanged hostname or unset UYUNI_HOSTNAME`, it usually means hostname is already set.

#### Step 4: Regenerate bootstrap for new host

```bash
kubectl -n uyuni exec deploy/uyuni -- \
  rhn-bootstrap --activation-keys=1-opensuse_micro6_1-x86_64 \
  --force-bundle --script=bootstrap-micro.sh --hostname=uyuni.home.arpa -v
```

#### Step 5: Force minion repo regeneration

```bash
kubectl -n uyuni exec deploy/micro-client -- sh -lc \
  'grep -q "uyuni.home.arpa" /etc/hosts || echo "192.168.116.69 uyuni.home.arpa" >> /etc/hosts'
kubectl -n uyuni exec deploy/uyuni -- salt 'micro-client' saltutil.refresh_pillar
kubectl -n uyuni exec deploy/uyuni -- salt 'micro-client' cmd.run 'rm -f /etc/zypp/repos.d/susemanager:channels.repo'
kubectl -n uyuni exec deploy/uyuni -- salt 'micro-client' state.apply channels
kubectl -n uyuni exec deploy/micro-client -- sh -lc "grep '^baseurl=' /etc/zypp/repos.d/susemanager:channels.repo"
```

Then retry:

```bash
kubectl -n uyuni exec deploy/uyuni -- salt 'micro-client' pkg.refresh_db
```

---

### Problem F: `spacewalk-remove-channel ... no such option: -y`

What it is:

- command line option mismatch

Why it happens:

- `spacewalk-remove-channel` does not implement a `-y` flag

Why the fix works:

- removing unsupported flags lets the tool parse options and execute normally

Use command without `-y`.

---

### Problem G: UI reachable but package refresh still failing

What it is:

- Uyuni UI works, but client package metadata refresh fails

Why it happens:

- server FQDN, pillar values, and generated repo baseurls are inconsistent
- this usually happens after hostname/FQDN changes, partial re-registration, or stale repo files

Why this check sequence works:

- it validates configuration from source (`rhn.conf`) -> policy (`pillar`) -> client output (`channels.repo`)
- if these three match, metadata URLs resolve consistently and `pkg.refresh_db` succeeds

Check in order:

1. server hostname

```bash
kubectl -n uyuni exec deploy/uyuni -- sh -lc "grep '^java.hostname' /etc/rhn/rhn.conf"
```

2. minion pillar host values

```bash
kubectl -n uyuni exec deploy/uyuni -- sh -lc "salt 'micro-client' pillar.get mgr_server"
kubectl -n uyuni exec deploy/uyuni -- sh -lc "salt 'micro-client' pillar.get channels"
```

3. actual minion repo baseurl

```bash
kubectl -n uyuni exec deploy/micro-client -- sh -lc "grep '^baseurl=' /etc/zypp/repos.d/susemanager:channels.repo"
```

All three should consistently show your public lab FQDN (`uyuni.home.arpa` in this guide).

---

### Problem H: Bootstrap finished but `test.ping` says "No minions matched the target"

What it is:

- Salt minion process is not correctly connected/identified, so Uyuni cannot target it

Symptom:

```text
No minions matched the target. No command was sent, no jid was assigned.
```

Why it happens in this guide:

- `micro-client` is an in-cluster container test pod (not systemd VM)
- bootstrap does not fully start minion daemon automatically
- minion id can be empty if `hostname` utility is missing

Why this fix works:

- writes deterministic minion identity (`minion_id`)
- points transport to reachable in-cluster salt endpoint (`salt.uyuni.svc.cluster.local`)
- starts daemon directly without systemd
- accepts server-side key so command bus can target the minion

Fix (exact sequence):

```bash
kubectl -n uyuni exec deploy/micro-client -- sh -lc '
  printf "micro-client\n" > /etc/venv-salt-minion/minion_id
  sed -i "s/^master:.*/master: salt.uyuni.svc.cluster.local/" /etc/venv-salt-minion/minion.d/susemanager.conf
  pkill -f "^/usr/lib/venv-salt-minion/bin/python.original /usr/bin/venv-salt-minion -d$" || true
  /usr/bin/venv-salt-minion -d
'
kubectl -n uyuni exec deploy/uyuni -- salt-key -a micro-client -y
kubectl -n uyuni exec deploy/uyuni -- salt 'micro-client' test.ping
```

---

### Problem I: Salt key accepted and `test.ping` works, but system is missing in UI

What it is:

- transport-level Salt connectivity exists, but Uyuni system onboarding fails

Symptoms:

- `Systems > System List > All` is empty
- server log shows registration errors like:
  - `null value in column "digital_server_id" of relation "rhnserver"`
  - `Activation Key ... is not valid for minionId ...`

Why it happens:

- minimal container image can have empty `/etc/machine-id`
- then Salt grain `machine_id` is empty
- Uyuni cannot create a valid `rhnServer` row (digital server identifier resolves to null), so system profile is not created in UI

Why this fix works:

- generating machine-id restores required identity grain
- minion reconnect emits fresh registration event with non-empty identity
- Uyuni can persist system profile and show it in UI

Fix:

```bash
kubectl -n uyuni exec deploy/micro-client -- sh -lc '
  MID=$(cat /etc/machine-id 2>/dev/null || true)
  if [ -z "$MID" ]; then
    cat /proc/sys/kernel/random/uuid | tr -d "-" > /etc/machine-id
  fi
  /usr/bin/venv-salt-call --local grains.item machine_id host fqdn
  pkill -f "^/usr/lib/venv-salt-minion/bin/python.original /usr/bin/venv-salt-minion -d$" || true
  /usr/bin/venv-salt-minion -d
'
kubectl -n uyuni exec deploy/uyuni -- salt 'micro-client' test.ping
```

Then refresh UI and check:

- `Systems > Recently Registered`
- `Systems > System List > All`

If system appears but channels are empty, fix/replace activation key assignment, then rerun:

```bash
export UYUNI_PASS='ChangeMeAdmin123!'
kubectl -n uyuni exec deploy/uyuni -- \
  spacecmd -y -u admin -p "$UYUNI_PASS" system_setbasechannel micro-client opensuse_micro6_1-x86_64
kubectl -n uyuni exec deploy/uyuni -- \
  spacecmd -y -u admin -p "$UYUNI_PASS" system_addchildchannels micro-client opensuse_micro6_1-uyuni-client-x86_64
kubectl -n uyuni exec deploy/uyuni -- salt 'micro-client' state.apply channels
kubectl -n uyuni exec deploy/uyuni -- salt 'micro-client' pkg.refresh_db
```

If you hit:

```text
The function "state.apply" is running ...
```

another state run is already active on that minion. Check and wait until it clears:

```bash
kubectl -n uyuni exec deploy/uyuni -- salt 'micro-client' saltutil.running
kubectl -n uyuni exec deploy/uyuni -- salt-run jobs.active
```

Then rerun:

```bash
kubectl -n uyuni exec deploy/uyuni -- salt 'micro-client' state.apply channels
kubectl -n uyuni exec deploy/uyuni -- salt 'micro-client' pkg.refresh_db
```

---

## 12. Handling IP/Network Changes (Wi-Fi Roaming)

If you connect to a different Wi-Fi network, your machine's IP changes. This breaks
**three things**:

1. **etcd** — RKE2's embedded etcd stores the node IP in its member list. If the IP
   changes, etcd refuses to start and RKE2 fails entirely.
2. **DNS mappings** — CoreDNS and pod `/etc/hosts` entries still point to the old IP.
3. **Salt minion** — may need restart to reconnect.

### Automated fix: `fix-uyuni-ip.sh`

A script at `~/fix-uyuni-ip.sh` handles all of this automatically:

```bash
~/fix-uyuni-ip.sh
```

It auto-detects your current IP and:
1. Updates host `/etc/hosts`
2. Resets etcd and restarts RKE2 if it's down (handles `--cluster-reset`)
3. Updates CoreDNS configmap
4. Updates `/etc/hosts` inside Uyuni and micro-client pods
5. Restarts salt-minion in micro-client (or restores from persistent volume)
6. Verifies salt connectivity

You can also pass an IP manually: `~/fix-uyuni-ip.sh 192.168.1.42`

### Manual fix (if script is unavailable)

#### Step 1: Get your new IP

```bash
ip -4 route get 1.1.1.1 | awk '{for(i=1;i<=NF;i++) if ($i=="src") print $(i+1)}'
export NEW_IP=<your-new-ip>
```

#### Step 2: Update host `/etc/hosts`

```bash
sudo sed -i '/uyuni.home.arpa/d' /etc/hosts
echo "$NEW_IP uyuni.home.arpa" | sudo tee -a /etc/hosts
```

#### Step 3: Fix RKE2/etcd if cluster is down

```bash
# Check if RKE2 is running
sudo systemctl is-active rke2-server

# If not running: reset etcd, remove flag, start
sudo rke2 server --cluster-reset 2>&1 | tail -5
sudo rm -f /var/lib/rancher/rke2/server/db/reset-flag
sudo systemctl start rke2-server

# Wait for cluster
export KUBECONFIG=/etc/rancher/rke2/rke2.yaml
kubectl get nodes   # retry until it works (~1-2 min)
```

#### Step 4: Update CoreDNS

```bash
kubectl -n kube-system get configmap rke2-coredns-rke2-coredns -o yaml | \
  sed "s/[0-9]\{1,3\}\.[0-9]\{1,3\}\.[0-9]\{1,3\}\.[0-9]\{1,3\} uyuni.home.arpa/$NEW_IP uyuni.home.arpa/" | \
  kubectl apply -f -
kubectl -n kube-system rollout restart deploy/rke2-coredns-rke2-coredns
```

#### Step 5: Update pod `/etc/hosts`

Note: `sed -i` fails on Kubernetes-mounted `/etc/hosts`. Use `cat` redirect instead:

```bash
for deploy in uyuni micro-client; do
  kubectl -n uyuni exec deploy/$deploy -- sh -c \
    "grep -v 'uyuni.home.arpa' /etc/hosts > /tmp/hosts.new && cat /tmp/hosts.new > /etc/hosts && echo '$NEW_IP uyuni.home.arpa' >> /etc/hosts"
done
```

#### Step 6: Restart salt-minion and verify

```bash
kubectl -n uyuni exec deploy/micro-client -- sh -c '/usr/bin/venv-salt-minion -d' 2>/dev/null || true
kubectl -n uyuni exec deploy/uyuni -- salt 'micro-client' test.ping
```

### If on macOS too (SSH/VS Code remote)

```bash
sudo sed -i '' '/uyuni.home.arpa/d' /etc/hosts
echo "$NEW_IP uyuni.home.arpa" | sudo tee -a /etc/hosts
sudo dscacheutil -flushcache && sudo killall -HUP mDNSResponder
```

---

## 13. Quick Health Checklist (Copy/Paste)

Use this any time after reboot or network changes:

```bash
export KUBECONFIG=/etc/rancher/rke2/rke2.yaml

kubectl get nodes
kubectl -n uyuni get pods
kubectl -n uyuni exec deploy/uyuni -- sh -lc "grep '^java.hostname' /etc/rhn/rhn.conf"
kubectl -n uyuni exec deploy/uyuni -- salt 'micro-client' test.ping
kubectl -n uyuni exec deploy/uyuni -- salt 'micro-client' pkg.refresh_db
```

If all pass, your local lab is healthy.

---

## 14. What You Can Do Next (GSoC Prep)

Once setup is stable, good next steps are:

1. create a small reproducible issue/fix loop in Uyuni codebase
2. document one troubleshooting flow with logs and exact fix
3. practice adding tests for your change area
4. share your setup notes in your GSoC discussion thread

That demonstrates environment readiness and contributor discipline.
