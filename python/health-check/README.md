### mgr-health-check

A tool providing dashboard, metrics and logs from an Uyuni Server supportconfig to visualize its health status.

## Requirements

* `python3`
* `podman`

## Building and installing

Install the tool locally into a virtual environment:

```
python3 -m venv venv
. venv/bin/activate
pip install .
```

## Getting started

This tool builds and deploys the necessary containers to scrape some metrics and logs from an Uyuni Server supportconfig directory.
Execute the `start` phase of the tool as such:

#### Analyze logs from the last 15 days

```console
mgr-health-check -s ~/path/to/supportconfig start --since 15
```

#### Analyze logs from a custom datetime range

``` console
mgr-health-check -s ~/path/to/supportconfig start --from_datetime=2024-01-01T00:00:00Z --to_datetime=2024-06-01T20:00:00Z
```

This will create and start the following containers locally:

- health-exporter (port `9000`)
- grafana (port `3000`)
- loki (port `9100`)
- promtail (port `9081`)

After you start the containers, visit `localhost:3000` and select the `Supportconfig with Logs` dashboard.
If necessary, the default username/password for Grafana is `admin:admin`.

To turn down the containers and analyze another supportconfig, you must stop your previous environment first:

```console
mgr-health-check stop
```

## Security notes
After running this tool, and until containers are destroyed, the Grafana Dashboards (and other metrics) are exposing metrics and logs messages that may contain sensitive data and information to any non-root user in the system or to anyone that have access to this host in the network.
