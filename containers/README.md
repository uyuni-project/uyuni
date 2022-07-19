## Install on k3s with MetalLB

Install `k3s` without load balancer and traefik router:

    curl -sfL https://get.k3s.io | sh -s - server --disable=traefik --disable=servicelb

Copy `kubectl config view --flatten=true` output to the work machine and change the IP address to the proxy host FQDN.

Run `export KUBECONFIG=/path/to/dev-kubeconfig`.

Install metalLB:

    helm repo add metallb https://metallb.github.io/metallb

Create a `metallb-values.yaml` with content like the following with adjusted IP addresses:

```yaml
    configInline:
      address-pools:
       - name: default
         protocol: layer2
         addresses:
           - 192.168.122.240-192.168.122.250
```

Run the following command to install metalLB:

    helm install metallb metallb/metallb -f metallb-values.yaml 
Create a `custom-values.yaml` file with the following content:

    services:
      annotations:
        metallb.universe.tf/allow-shared-ip: key-to-share-ip
        metallb.universe.tf/loadBalancerIPs: 192.168.122.241

Add a `dev-pxy-k3s.mgr.lab` DNS entry for `192.168.122.241`

Deploy the proxy helm chart:

    tar -x -C test -f ~/Downloads/dev-pxy-k3s-config.tar.gz
    helm upgrade --install proxy ./proxy-helm -f test/config.yaml -f test/httpd.yaml -f test/ssh.yaml -f custom-values.yaml
