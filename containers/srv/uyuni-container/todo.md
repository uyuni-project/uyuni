- run in kubernetes
- use volumes to store data and recreate
- clean system variables
- split in different containers

# run in kubernetes with minikube
```
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

- That is the output
{"data":"RESTful with two Endpoint /users and /notes - v1.0.0"}


*****************************************
k3s kubectl ...

k3s kubectl create -f hub-opensuse152.yml
k3s kubectl get po -A
k3s kubectl describe pod  hub-opensuse152
k3s kubectl delete po hub-opensuse152

https://dev.to/fransafu/the-first-experience-with-k3s-lightweight-kubernetes-deploy-your-first-app-44ea

## create ymal configuration file
podman pod create -n hub-opensuse152 -p 4505:4505 -p 4506:4506 -p 443:443 -p 5432:5432 --hostname hub-opensuse152

podman run --pod hub-opensuse152 -ti --name hub-opensuse152-server localhost/uyuni

podman generate kube hub-opensuse152  > hub-opensuse152.yml

open file and remove the last empty lines
> change image to be load from localhost:5000/uyuni

podman pod rm hub-opensuse152


## install minikube
https://minikube.sigs.k8s.io/docs/start/
curl -LO https://storage.googleapis.com/minikube/releases/latest/minikube-latest.x86_64.rpm
sudo rpm -ivh minikube-latest.x86_64.rpmdes

## start minikube
minikube start  --force --driver=podman

minikube kubectl -- get po -A

## image deployment?


## deployment and management
minikube kubectl -- delete po hub-opensuse152

minikube kubectl -- create -f hub-opensuse152.yml

minikube kubectl -- get po -A

minikube kubectl -- describe pod  hub-opensuse152











---

# build image to minikube
https://minikube.sigs.k8s.io/docs/handbook/pushing/#3-pushing-directly-to-in-cluster-cri-o-podman-env

# local registry
https://hasura.io/blog/sharing-a-local-registry-for-minikube-37c7240d0615/

podman run -p 5000:5000 registry

find contaniner ip address
> podman inspect -f '{{ .NetworkSettings.IPAddress }}' <Contanier_id>

- edit file
/etc/containers/registries.conf
add local image to the registry untrust
---------------
kube-registry.yaml: https://gist.github.com/coco98/b750b3debc6d517308596c248daf3bb1

minikube kubectl -- create -f kube-registry.yaml

minikube kubectl -- get po -n kube-system | grep kube-registry-v0 | \awk '{print $1;}'
-out put goes to:
minikube kubectl -- port-forward --namespace kube-system kube-registry-v0-qbclg 5000:5000

# run in kubernetes
--- share image
- build image
sudo podman build -t uyuni .

-- tag image
podman tag uyuni localhost:5000/uyuni
podman push localhost:5000/uyuni

---

--
