Postgresql server image for Uyuni.
This image adds init scripts that are specific to Uyuni.

This image cannot be used independently.
Refer to the Uyuni server installation documentation either on [podman](https://www.uyuni-project.org/uyuni-docs/en/uyuni/installation-and-upgrade/container-deployment/uyuni/server-deployment-uyuni.html) or [Kubernetes](https://www.uyuni-project.org/uyuni-docs/en/uyuni/specialized-guides/kubernetes-guide/server-kubernetes-deployment.html).

## Container Startup and Lifecycle Management

Container initialization and runtime health are managed through the interaction of `diskcheck.sh` script, container healthchecks, and the `uyuni-db-server.service` systemd unit. During early startup, the entrypoint `uyuni-entrypoint.sh` executes `/usr/bin/diskcheck.sh` to check for critically low disk space and aborts startup with a non-zero exit code (1) if space is exhausted. This ensures the systemd service (configured with `Restart=on-success`) halts immediately in a failed state rather than restarting endlessly on a full disk. Conversely, if a periodic healthcheck fails during runtime, Podman's `--health-on-failure=stop` flag gracefully stops the container (exit code 0), prompting systemd to automatically restart it and attempt recovery; any persistent disk exhaustion will then be caught and cleanly halted during the subsequent startup's early disk check.
