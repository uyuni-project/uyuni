# Podman Test Runner

This directory contains scripts for running the Uyuni test suite inside Podman containers.
These scripts are designed to be executed sequentially as part of a GitHub workflow or manually from your local development workstation.

## Prerequisites

### Required Software

You must have **Podman** and **Docker** installed and available on the host where you execute the `./run` command.

- **Linux**: Install Podman/Docker using your distribution's package manager
- **macOS**: Install Podman/Docker Desktop or use Homebrew (`brew install podman/docker`)
- **Windows**: Install Podman/Docker Desktop

During the execution of the scripts, it will install nginx in your system.
Last but not least, you must also have access to the Uyuni project and OpenSUSE mirrors on the internet.

Podman may need to be configured to use `localhost:5001` and `localhost:5002` as insecure ones.
Add the following to a `/etc/containers/registries.conf.d/999-ci-runner.conf` file:

```toml[[registry]]
location = "localhost:5002"
insecure = true

[[registry]]
location = "localhost:5001"
insecure = true

```

### Required Environment Variables

Before running these scripts, you must export the following environment variables:
```
bash
export UYUNI_PROJECT=<project-name>
export UYUNI_VERSION=<version>
```
These variables are required to download the correct container images.

## Quick Start

### Run All Tests

To execute all tests in sequence:
```
bash
./run
```
### Run Individual Scripts

You can also execute scripts individually:
```
bash
./00_clean_env.sh
./01_setup_tmp_dirs.sh
# ... and so on
```
## Script Overview

The scripts are numbered to indicate their execution order:

### Environment Setup (00-06)
- **00_clean_env.sh** - Clean up previous test environments
- **01_setup_tmp_dirs.sh** - Create temporary directories
- **02_setup_network.sh** - Configure networking for containers
- **03_run_controller_and_registry_and_buildhost.sh** - Start controller, registry, and buildhost containers
- **03_run_controller_and_registry_and_buildhost_darwin.sh** - macOS-specific version
- **04_setup_ssh_controller.sh** - Configure SSH access to the controller
- **05_install_gems_in_controller.sh** - Install Ruby gems in the controller
- **06_collect_and_tag_flaky_tests_in_controller.sh** - Identify and tag flaky tests

### Server Setup (07-09)
- **07_server_setup.sh** - Configure the test server
- **08_start_server.sh** - Start the server container
- **09_build_server_code.sh** - Build server-side code

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
```
bash
podman ps
```
### Connect to a Container

To access a running container:
```
bash
podman exec -ti <container-name> bash
```
For example, to connect to the controller:
```
bash
podman exec -ti controller bash
```
### Run Specific Tests

Once connected to the controller container, you can run specific tests:
```
bash
cucumber path/to/test_feature.feature
```
## Helper Scripts

Additional utility scripts are available:

- **generate_certificates.sh** - Generate SSL certificates
- **provide-db-schema.sh** - Provide database schema
- **run_db_migrations.sh** - Run database migrations
- **run_redis.sh** - Start Redis container
- **salt-minion-entry-point.sh** - Entry point for Salt minions
- **setup-nginx-proxy-for-docker-registries.sh** - Configure nginx proxy
- **setup_missing_folders.sh** - Create any missing directories
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
- **server-all-in-one-dev/** - All-in-one development server
- **ubuntu-minion/** - Ubuntu minion
- **uyuni-master-testsuite/** - Master test suite container

## Troubleshooting

### Logs

Check container logs using:

```bash
podman logs <container-name>
```
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
