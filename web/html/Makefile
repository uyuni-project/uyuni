# Makefile for installing the html files for RHN
#

TOP = ..

# Project defines
INSTALL_DEST	= /srv/www/htdocs

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
