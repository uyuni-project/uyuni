# Makefile for src
#

# what is the backend top dir
TOP     = ..

# Specific stuff
SUBDIR		= susemanager

SPACEWALK_FILES		= mgr_ncc_sync_lib __init__ \
                          suseLib mgr_register
SCRIPTS  = mgr-ncc-sync mgr-register

SBINDIR  = /usr/sbin

EXTRA_DIRS = $(SBINDIR)

include $(TOP)/Makefile.defs

# install scripts
all :: $(SCRIPTS)
install :: $(SCRIPTS) $(PREFIX)/$(SBINDIR)
	$(INSTALL_BIN) $(SCRIPTS) $(PREFIX)/$(SBINDIR)
