# Known issues

* Avahi names are not resolved inside the container

## Container Startup and Lifecycle Management

Container initialization and runtime health are managed through the interaction of `00-diskcheck.sh`, container healthchecks, and the `uyuni-server.service` systemd unit. During early startup, the entry point script `00-diskcheck.sh` checks for critically low disk space and aborts the initialization with a non-zero exit code (1) if space is exhausted, causing the systemd service (configured with `Restart=on-success`) to stop immediately in a failed state rather than restarting endlessly. Conversely, if the system is running and a periodic healthcheck fails, Podman's `--health-on-failure=stop` flag triggers a graceful stop (exit code 0), allowing systemd to automatically restart the service and attempt recovery; if the underlying issue is a full disk, the subsequent restart will be safely caught and halted by the early `00-diskcheck.sh` startup check.
