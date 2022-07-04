# General usage

In order to run the containers, generate a configuration using one of:

* `spacecmd proxy_container_config` command,
* `spacecmd proxy_container_config_generate_cert` command
* or the web UI

Unpack the configuration file in `/etc/uyuni/proxy` and start the services by running `systemctl start pod-proxy-pod.service`.


# Advanced options

In order to change the default images registry, namespace and tag, edit the `NAMESPACE` and `TAG` variables in `/etc/sysconfig/uyuni-proxy-systemd-services` file.
Restart the `uyuni-proxy-pod` service is required to apply the change.
