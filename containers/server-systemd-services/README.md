# General usage

Start the services by running `systemctl start uyuni-server.service`.

Edit the `/etc/sysconfig/uyuni-server-systemd-services` file if you need to add more options to the `podman` pod running command.

# Advanced options

In order to change the default images registry, namespace and tag, edit the `NAMESPACE` and `TAG` variables in `/etc/sysconfig/uyuni-server-systemd-services` file.
Restart the `uyuni-server` service is required to apply the change.

# Getting logs

You can get logs from the `journalctl -xeu uyuni-server.service` services using `journalctl`.
You can also use `podman logs` using the same names.

# Limitations
- get rid of some mounts and include them into the image
- hostname and REPORT_DB_HOST should be configurable by the user
