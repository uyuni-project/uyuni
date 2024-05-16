# uyuni-storage-setup

This project contains a few scripts to help set up additional storage
for SUSE Manager and Uyuni.
It is not a replacement for `mgradm` or `mgrpxy`.

This repository contains scripts to set up a storage device for use with
SUSE Manager and Uyuni. There are two versions, the server and the proxy
version. Both versions partition and fromat the given block device and
mount it to the system, The server version is for Uyuni or SUSE Manager Server
and moves all container volumes to the given device storage devide.
When a database storage is provided, the database container volume is moved
to this extra device.
The proxy version is for Uyuni or SUSE Manager Proxy and moves all container
volumes to the given device.

These scripts are not limited to the public cloud use case, but are also
useful in a standard Datacenter and VM installation.

## Installation
```
make install
```

## Usage

```
mgr-storage-[server|proxy] block_device
```

