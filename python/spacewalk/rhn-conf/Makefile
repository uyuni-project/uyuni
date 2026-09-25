# SPDX-FileCopyrightText: 2026 Red Hat, Inc.
# SPDX-FileCopyrightText: 2026 SUSE LLC
#
# SPDX-License-Identifier: GPL-2.0-only

# Makefile for installation of the RHN server configuration files
#

# what is the backend top dir
TOP	= ..

INSTALL_FILES	= $(wildcard *.conf)
INSTALL_DEST	= /usr/share/rhn/config-defaults

include $(TOP)/Makefile.defs

install :: $(PREFIX)$(INSTALL_DEST)
