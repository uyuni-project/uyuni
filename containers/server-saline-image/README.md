Salt prometheus exporter image.

This image is meant to be used with Uyuni server but may work with another Salt master.
The container running this image needs to access the `/run/salt/master` folder of the Salt master to work.

Refer to the Uyuni server installation documentation either on [podman](https://www.uyuni-project.org/uyuni-docs/en/uyuni/installation-and-upgrade/container-deployment/uyuni/server-deployment-uyuni.html) or [Kubernetes](https://www.uyuni-project.org/uyuni-docs/en/uyuni/specialized-guides/kubernetes-guide/server-kubernetes-deployment.html).
