#!/bin/bash
set -e
export KUBECONFIG=/etc/rancher/rke2/rke2.yaml
FQDN="uyuni.home.arpa"

NEW_IP=$(ip -4 route get 1.1.1.1 2>/dev/null | awk '{for(i=1;i<=NF;i++) if ($i=="src") print $(i+1)}')
if [ -z "$NEW_IP" ]; then
  echo "Could not detect IP. Pass it as argument: $0 <ip>"
  exit 1
fi
[ -n "$1" ] && NEW_IP="$1"

echo "Updating $FQDN -> $NEW_IP"

# Host /etc/hosts
sudo sed -i "/$FQDN/d" /etc/hosts
echo "$NEW_IP $FQDN" | sudo tee -a /etc/hosts

# CoreDNS
kubectl -n kube-system get configmap rke2-coredns-rke2-coredns -o yaml | \
  sed "s/[0-9]\{1,3\}\.[0-9]\{1,3\}\.[0-9]\{1,3\}\.[0-9]\{1,3\} $FQDN/$NEW_IP $FQDN/" | \
  kubectl apply -f -
kubectl -n kube-system rollout restart deploy/rke2-coredns-rke2-coredns

# Uyuni pod
kubectl -n uyuni exec deploy/uyuni -- sh -c "sed -i '/$FQDN/d' /etc/hosts && echo '$NEW_IP $FQDN' >> /etc/hosts" 2>/dev/null || echo "Uyuni pod not running, skipping"

# micro-client pod
kubectl -n uyuni exec deploy/micro-client -- sh -c "sed -i '/$FQDN/d' /etc/hosts && echo '$NEW_IP $FQDN' >> /etc/hosts" 2>/dev/null || echo "micro-client pod not running, skipping"

echo "Done. Verify: curl -sk https://$FQDN/rhn/manager/api/api/getVersion"
