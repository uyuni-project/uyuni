# Podman Test Runner

This directory contains scripts for running the Uyuni test suite inside Podman containers.
These scripts are designed to be executed sequentially as part of a GitHub workflow or manually from your local development workstation.

## Prerequisites

### Required Software

You must have the following required host tools installed and available on your local operating system to execute `./run` or compile PR artifacts locally:

- **Java Development Kit (JDK 17)**: For compiling Java/Maven backend code
- **Apache Maven**: For dependency resolution and backend package compilation
- **Node.js (v22.x) and npm**: For resolving frontend dependencies and compiling the static bundles
- **Python 3 and pip**: For running Python overlays and `obs-to-maven` dependency synchronization
- **obs-to-maven**: A Python tool used to package OBS dependencies for local Maven repository layouts
- **Perl, Make, rsync, and tar**: For schema generation, directory synchronization, and payload packaging
- **Podman**: For managing the container runtime, volumes, networks, and secrets
- **Docker**: Optional, for container socket compatibility

### Local Tool Installation

1. Install `obs-to-maven` on your local host using `pip`:
   ```bash
   pip install git+https://github.com/uyuni-project/obs-to-maven.git@v1.1.13
   ```

2. Make sure Podman is configured to use `localhost:5001` and `localhost:5002` as insecure registries. Add the following to your `/etc/containers/registries.conf.d/999-ci-runner.conf` file:

```toml
[[registry]]
location = "localhost:5002"
insecure = true

[[registry]]
location = "localhost:5001"
insecure = true
```

### Required Environment Variables

Before running these scripts, you must export the following environment variables:
```bash
export UYUNI_PROJECT=<project-name>
export UYUNI_VERSION=<version>
```
These variables are required to download the correct container images.

## Quick Start

### Run All Tests

To execute all tests in sequence:
```bash
./run
```

This local `./run` script will:
1. Validate all prerequisites.
2. Build the Maven backend and JavaScript frontend.
3. Generate the Spacewalk and report database schemas.
4. Assemble the final deployment payload under `.build/acceptance-server-root` and package it into `.build/acceptance-payload.tar.gz`.
5. Build the thin PR image `ghcr.io/${UYUNI_PROJECT}/uyuni/ci-test-server-pr:${UYUNI_VERSION}` on top of the published base image using `./testsuite/dockerfiles/server-pr/Dockerfile`.
6. Set `TEST_IMAGE` to the thin PR image and run the official `/docker-entrypoint-init` command inside the container to perform fresh product installation and database population.

### Run Individual Scripts

You can also execute scripts individually:
```bash
./00_clean_env.sh
./01_setup_tmp_dirs.sh
# ... and so on
```

## Script Overview

The scripts are numbered to indicate their execution order:

### Environment Setup (00-06)
- **00_clean_env.sh** - Clean up previous test environments (idempotent; returns 0 if clean)
- **01_setup_tmp_dirs.sh** - Create temporary directories
- **02_setup_network.sh** - Configure networking for containers
- **03_run_controller_and_registry_and_buildhost.sh** - Start controller, registry, and buildhost containers
- **03_run_controller_and_registry_and_buildhost_darwin.sh** - macOS-specific version
- **04_setup_ssh_controller.sh** - Configure SSH access to the controller
- **05_install_gems_in_controller.sh** - Install Ruby gems in the controller
- **06_collect_and_tag_flaky_tests_in_controller.sh** - Identify and tag flaky tests safely

### Server Setup (07-08)
- **07_build_pr-changes.sh** - Build Maven/NPM, compile schemas, and assemble the `.build/pr-changes-payload` payload
- **08_server_setup.sh** - Configure SSL certificates and databases
- **09_start_server.sh** - Start the thin PR-specific server container running `/docker-entrypoint-init` and wait for it to become healthy (15 mins timeout)

### Minion Setup (10-14)
- **10_run_sshminion.sh** - Start SSH minion
- **11_setup_sshd.sh** - Configure SSH daemon
- **12_run_salt_sle_minion.sh** - Start SUSE Linux Enterprise Salt minion
- **13_run_salt_rhlike_minion.sh** - Start Red Hat-like Salt minion
- **14_run_salt_deblike_minion.sh** - Start Debian-like Salt minion

### Test Execution (15-23)
- **15_run_core_tests.sh** - Execute core test suite
- **16_accept_all_keys.sh** - Accept all Salt keys
- **17_run_init_clients_tests.sh** - Run client initialization tests
- **18_run_secondary_tests.sh** - Execute secondary test suite
- **19_generate_recommended_tests_yml.sh** - Generate recommended tests YAML
- **20_run_recommended_tests.sh** - Run recommended tests
- **21_run_secondary_parallelizable_tests.sh** - Execute parallelizable secondary tests
- **22_run_secondary_parallelizable_tests_subset.sh** - Run a subset of parallelizable tests
- **23_split_secondary_p_tests.sh** - Split secondary parallelizable tests

### Log Collection (24-25)
- **24_get_server_logs.sh** - Collect server logs
- **25_get_client_logs.sh** - Collect client logs

## Container Management

### View Running Containers
```bash
podman ps
```
### Connect to a Container

To access a running container:
```bash
podman exec -ti <container-name> bash
```
For example, to connect to the controller:
```bash
podman exec -ti controller bash
```
### Run Specific Tests

Once connected to the controller container, you can run specific tests:
```bash
cucumber path/to/test_feature.feature
```

## Helper Scripts

Additional utility scripts are available:

- **generate_certificates.sh** - Generate SSL certificates
- **run_redis.sh** - Start Redis container
- **salt-minion-entry-point.sh** - Entry point for Salt minions
- **setup-nginx-proxy-for-docker-registries.sh** - Configure nginx proxy
- **debug_logging.properties** - Logging configuration for debugging

## Dockerfiles

Container definitions are located in the `../dockerfiles/` directory and include:

- **buildhost/** - Build host container for package building
- **controller-dev/** - Development controller container
- **fakeipmi/** - Fake IPMI server for testing
- **opensuse/** - openSUSE base images (Leap 15.5, 15.6, Tumbleweed)
- **opensuse-minion/** - openSUSE minion
- **postgresql/** - PostgreSQL database
- **redis/** - Redis cache
- **rocky-minion/** - Rocky Linux minion
- **server-all-in-one-dev/** - All-in-one development base server
- **server-pr/** - Thin PR image builder extending base server using the build payload
- **ubuntu-minion/** - Ubuntu minion
- **uyuni-master-testsuite/** - Master test suite container

## Troubleshooting

### Logs

Check container logs using:

```bash
podman logs <container-name>
```


### Reset Environment

If you encounter issues, clean the environment and start fresh:

```shell script
./00_clean_env.sh
```


### Network Issues

If containers cannot communicate, verify the network setup:

```shell script
podman network ls
podman network inspect <network-name>
```


## Additional Resources

For more information about the test suite, refer to the main README in the `testsuite/` directory.
