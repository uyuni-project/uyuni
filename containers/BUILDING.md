# Building instructions for developers


## Pushing to the Open Build Service

```
osc branch systemsmanagement:Uyuni:Master server-image
export TEST=--test
export OSCAPI=https://api.opensuse.org
export OBS_PROJ=home:<yourobslogin>:branches:systemsmanagement:Uyuni:Master
build-packages-for-obs proxy-httpd-image proxy-salt-broker-image proxy-squid-image proxy-ssh-image proxy-tftpd-image server-image && push-packages-to-obs
```
