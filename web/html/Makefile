# SPDX-FileCopyrightText: 2026 SUSE LLC
#
# SPDX-License-Identifier: GPL-2.0-only

# Makefile for installing the html files for RHN
#

TOP = ..

# Project defines
INSTALL_DEST	= /usr/share/susemanager/www/htdocs

# common stuff
include $(TOP)/Makefile.defs

# exclude these files from instalation
NO_INSTALL	= .cvsignore Makefile _project OLD \
		  bench phantom project.html src \
		  .gitignore

all::

install:: $(PREFIX)/$(INSTALL_DEST)
	tar cpf - $(addprefix --exclude ,$(NO_INSTALL)) . | \
	    tar vxf - -C $(PREFIX)$(INSTALL_DEST)
