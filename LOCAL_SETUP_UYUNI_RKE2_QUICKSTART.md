# Uyuni Local Setup Quickstart (RKE2)

This is the short version: only essential steps + brief descriptions.

## 1) Prepare `kubectl` and cluster access
Use RKE2 `kubectl` and set kubeconfig so commands do not fall back to `localhost:8080`.

```bash
sudo ln -sf /var/lib/rancher/rke2/bin/kubectl /usr/local/bin/kubectl
echo 'export KUBECONFIG=/etc/rancher/rke2/rke2.yaml' >> ~/.zshrc
export KUBECONFIG=/etc/rancher/rke2/rke2.yaml
kubectl get nodes
```

## 2) Prepare Helm dependencies
Install/update chart dependencies used by `server-selfsigned`.

```bash
cd /home/<user>/Desktop/githubrepo/uyuni/uyuni-charts/server-selfsigned
helm repo add rancher-latest https://releases.rancher.com/server-charts/latest
helm repo add jetstack https://charts.jetstack.io
helm repo update
helm dependency update
```

## 3) Create `values-lab.yaml`
Set credentials, FQDN, and chart-compatible server image.

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

## 4) Deploy Uyuni
Install/upgrade chart and wait for setup job + server pod to become ready.

```bash
export KUBECONFIG=/etc/rancher/rke2/rke2.yaml
cd /home/<user>/Desktop/githubrepo/uyuni
kubectl create ns uyuni
helm upgrade --install uyuni ./uyuni-charts/server-selfsigned -n uyuni -f values-lab.yaml
kubectl -n uyuni get pods -w
```

## 5) Make FQDN reachable
Map your FQDN to ingress IP on systems that access Uyuni.

Linux host:
```bash
grep -q 'uyuni.home.arpa' /etc/hosts || echo "192.168.116.69 uyuni.home.arpa" | sudo tee -a /etc/hosts
```

macOS host:
```bash
sudo sh -c 'grep -q "uyuni.home.arpa" /etc/hosts || echo "192.168.116.69 uyuni.home.arpa" >> /etc/hosts'
sudo dscacheutil -flushcache
sudo killall -HUP mDNSResponder
```

## 6) Create minimal channels
Create smallest practical channel set for Leap Micro lab.

```bash
kubectl -n uyuni exec deploy/uyuni -- \
  spacewalk-common-channels -u admin -p 'ChangeMeAdmin123!' -a x86_64 \
  opensuse_micro6_1 opensuse_micro6_1-uyuni-client
```

## 7) Sync channels (sequential)
Run repo sync one after the other (single lock allowed).

```bash
kubectl -n uyuni exec deploy/uyuni -- sh -lc \
  'spacewalk-repo-sync -c opensuse_micro6_1-x86_64 && spacewalk-repo-sync -c opensuse_micro6_1-uyuni-client-x86_64'
```

If lock error appears:
```bash
kubectl -n uyuni exec deploy/uyuni -- pgrep -af '/usr/bin/spacewalk-repo-sync'
kubectl -n uyuni exec deploy/uyuni -- rm -f /run/spacewalk-repo-sync.pid
```

## 7.1) Create activation key (required before bootstrap)
Create key first, otherwise client may register but no channels will be attached.

UI:

1. `Systems` -> `Activation Keys` -> `Create Key`
2. Key label: `1-opensuse_micro6_1-x86_64`
3. Base channel: `opensuse_micro6_1-x86_64`
4. Child channel: `opensuse_micro6_1-uyuni-client-x86_64`
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

Important: for `spacecmd activationkey_create`, pass `-n opensuse_micro6_1-x86_64` (without org prefix).  
`spacecmd` prepends org id automatically; if you pass `1-opensuse...`, it creates `1-1-opensuse...`.

Use the same key in bootstrap command:

```bash
--activation-keys=1-opensuse_micro6_1-x86_64
```

## 8) Create tiny test client deployment (persistent)
Create in-cluster client with persistent storage so salt-minion survives pod restarts.

```bash
kubectl -n uyuni delete pod micro-client --ignore-not-found=true
kubectl -n uyuni delete deploy micro-client --ignore-not-found=true
kubectl apply -f micro-client-deployment.yaml
kubectl -n uyuni rollout status deploy/micro-client --timeout=120s
```

## 9) Generate and run bootstrap
Create bootstrap script from Uyuni and run it in test client.

```bash
kubectl -n uyuni exec deploy/uyuni -- \
  rhn-bootstrap --activation-keys=1-opensuse_micro6_1-x86_64 \
  --force-bundle --script=bootstrap-micro.sh --hostname=uyuni.home.arpa -v

kubectl -n uyuni exec deploy/micro-client -- sh -lc \
  'grep -q "uyuni.home.arpa" /etc/hosts || echo "192.168.116.69 uyuni.home.arpa" >> /etc/hosts'

kubectl -n uyuni exec deploy/micro-client -- sh -lc \
  'curl -k https://uyuni.home.arpa/pub/bootstrap/bootstrap-micro.sh -o /tmp/bootstrap.sh && sh /tmp/bootstrap.sh'
```

## 10) Container-only post-bootstrap fix
For container clients, set machine-id/minion-id, point to in-cluster Salt service, and start minion.

```bash
kubectl -n uyuni exec deploy/micro-client -- sh -lc '
  MID=$(cat /etc/machine-id 2>/dev/null || true)
  if [ -z "$MID" ]; then
    cat /proc/sys/kernel/random/uuid | tr -d "-" > /etc/machine-id
  fi
  printf "micro-client\n" > /etc/venv-salt-minion/minion_id
  sed -i "s/^master:.*/master: salt.uyuni.svc.cluster.local/" /etc/venv-salt-minion/minion.d/susemanager.conf
  pkill -f "^/usr/lib/venv-salt-minion/bin/python.original /usr/bin/venv-salt-minion -d$" || true
  /usr/bin/venv-salt-minion -d
'

# Persist salt binary and machine-id so they survive pod restarts
kubectl -n uyuni exec deploy/micro-client -- sh -c '
  cp /usr/bin/venv-salt-minion /opt/salt-persist/venv-salt-minion 2>/dev/null || true
  cp /etc/machine-id /opt/salt-persist/machine-id 2>/dev/null || true
'

kubectl -n uyuni exec deploy/uyuni -- salt-key -a micro-client -y
kubectl -n uyuni exec deploy/uyuni -- salt 'micro-client' test.ping
```

## 11) Apply channels to client and refresh metadata
Generate repo file and test package metadata refresh.

```bash
kubectl -n uyuni exec deploy/uyuni -- salt 'micro-client' saltutil.refresh_pillar
kubectl -n uyuni exec deploy/uyuni -- salt 'micro-client' state.apply channels
kubectl -n uyuni exec deploy/uyuni -- salt 'micro-client' pkg.refresh_db
```

If you get `The function "state.apply" is running ...`, another state run is still active.  
Wait until it clears and rerun:

```bash
kubectl -n uyuni exec deploy/uyuni -- salt 'micro-client' saltutil.running
kubectl -n uyuni exec deploy/uyuni -- salt 'micro-client' state.apply channels
kubectl -n uyuni exec deploy/uyuni -- salt 'micro-client' pkg.refresh_db
```

If channels were not attached during bootstrap, force-attach them and rerun:

```bash
export UYUNI_PASS='ChangeMeAdmin123!'
kubectl -n uyuni exec deploy/uyuni -- \
  spacecmd -y -u admin -p "$UYUNI_PASS" system_setbasechannel micro-client opensuse_micro6_1-x86_64
kubectl -n uyuni exec deploy/uyuni -- \
  spacecmd -y -u admin -p "$UYUNI_PASS" system_addchildchannels micro-client opensuse_micro6_1-uyuni-client-x86_64
kubectl -n uyuni exec deploy/uyuni -- salt 'micro-client' state.apply channels
kubectl -n uyuni exec deploy/uyuni -- salt 'micro-client' pkg.refresh_db
```

## 12) Handle IP/network changes
When you switch Wi-Fi, your IP changes and RKE2/etcd/DNS all break.  
Run the fix script (auto-detects new IP, resets etcd if needed, updates everything):

```bash
~/fix-uyuni-ip.sh
```

Or manually pass an IP: `~/fix-uyuni-ip.sh 192.168.1.42`

See Section 12 of `LOCAL_SETUP_UYUNI_RKE2.md` for manual steps.

## 13) Quick health check
Use this any time after reboot or network changes.

```bash
export KUBECONFIG=/etc/rancher/rke2/rke2.yaml
kubectl get nodes
kubectl -n uyuni get pods
kubectl -n uyuni exec deploy/uyuni -- salt 'micro-client' test.ping
kubectl -n uyuni exec deploy/uyuni -- salt 'micro-client' pkg.refresh_db
```
