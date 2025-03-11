### uyuni-health-check

A tool providing dashboard, metrics and logs from an Uyuni server supportconfig to visualise its health status.

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

This tool builds and deploys the necessary containers to scrape some metrics and logs from an Uyuni server supportconfig directory.
Execute the `run` phase of the tool as such:

```
uyuni-health-check -s ~/path/to/supportconfig run --logs --from_datetime=2024-01-01T00:00:00Z --to_datetime=2024-06-01T20:00:00Z
```

This will create and start the following containers locally:

- uyuni-health-exporter (port `9000`)
- grafana (port `3000`)
- loki (port `9100`)
- promtail (port `9081`)

After you start the containers, visit `localhost:3000` and select the `Supportconfig with Logs` dashboard.
If necessary, the default username/password for Grafana is `admin:admin`.

## Security notes
After running this tool, and until containers are destroyed, the Grafana Dashboards (and other metrics) are exposing metrics and logs messages that may contain sensitive data and information to any non-root user in the system or to anyone that have access to this host in the network.

