## Prerequisites
 - a Kubernetes cluster. To install [k3s](https://k3s.io/):
```
curl -sfL https://get.k3s.io | sh -
```
 - Helm. To install:
```
curl https://raw.githubusercontent.com/helm/helm/master/scripts/get-helm-3 | bash
```

## Installation
```
sudo su
export KUBECONFIG=/etc/rancher/k3s/k3s.yaml
helm install postgres ./path/to/helm/postgres
```