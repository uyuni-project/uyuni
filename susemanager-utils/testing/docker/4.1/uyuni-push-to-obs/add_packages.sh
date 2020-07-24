#!/bin/bash -e

# Requirements to build the SRPMs and push them
zypper --non-interactive in osc \
             cpio \
             git \
             tito \
	     build \
	     npm \
	     ca-certificates-suse
