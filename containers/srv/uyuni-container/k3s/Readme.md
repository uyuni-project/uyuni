# run in kubernetes with k3s

## Import container image in k3s
```
sudo podman build -t uyuni .
podman image save localhost/uyuni > uyuni.tar
curl -sfL https://get.k3s.io | sh -
k3s ctr image import uyuni.tar
```

- Check if response the node with the current version
`k3s kubectl get node`

- Create the namespace (only dev)
`kubectl create namespace uyuni`

- Create ingress, service, and deployment
`k3s kubectl apply -f uyuni.yaml`

- Check ingress (its show your IP), service and pods
`k3s kubectl get ingress,svc,pods -n uyuni`

- Test cluster and app
curl -X GET http://YOUR_IP

## troubleshooting 
https://kubernetes.io/docs/tasks/debug-application-cluster/debug-running-pod/
