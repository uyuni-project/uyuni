# General usage

In order to run the containers, generate a configuration using one of:

* `spacecmd proxy_container_config` command,
* `spacecmd proxy_container_config_generate_cert` command
* or the web UI

Unpack the configuration file in `/etc/uyuni/proxy` and start the services by running `systemctl start pod-proxy-pod.service`.

Edit the `/etc/sysconfig/uyuni-proxy-systemd-services` file if you need to add more options to the `podman` pod running command.

# Advanced options

In order to change the default images registry, namespace and tag, edit the `NAMESPACE` and `TAG` variables in `/etc/sysconfig/uyuni-proxy-systemd-services` file.
Restart the `uyuni-proxy-pod` service is required to apply the change.

# Required volumes

In order to persist the caches and tftp boot files, the pod uses volumes.
Those are automatically created when the containers are started, but you can create and populate them in advance.
The volume names are:

* uyuni-proxy-squid-cache
* uyuni-proxy-rhn-cache
* uyuni-proxy-tftpboot

See the `podman-volume-create` and `podman-volume-import` man pages for more information on how to create custom volumes.

# Getting logs

You can get logs from the `uyuni-proxy-*` services using `journalctl`.
You can also use `podman logs` using the same names.
