# Makefile for src
#

# what is the backend top dir
TOP     = ..

# Specific stuff
SUBDIR		= susemanager

SPACEWALK_FILES		= mgr_ncc_sync_lib __init__ \
                          mgr_register mgr_clean_old_patchnames \
                          simpleproduct
SCRIPTS  = mgr-ncc-sync mgr-register mgr-clean-old-patchnames mgr-create-bootstrap-repo

SUMA_DATA_FILES = mgr_bootstrap_data

PYSUMA_DATA_FILES       = $(addsuffix .py,$(SUMA_DATA_FILES))
PYCSUMA_DATA_FILES      = $(addsuffix .pyc,$(SUMA_DATA_FILES))

SUMA_INSTALL_DATA_FILES += $(PYSUMA_DATA_FILES) $(PYCSUMA_DATA_FILES)

SBINDIR  = /usr/sbin

EXTRA_DIRS = $(SBINDIR)

include $(TOP)/Makefile.defs

# install scripts
all :: $(SCRIPTS)
install :: $(SCRIPTS) $(PREFIX)/$(SBINDIR) $(SUMA_INSTALL_DATA_FILES)
	$(INSTALL_BIN) $(SCRIPTS) $(PREFIX)/$(SBINDIR)
	$(INSTALL_DIR) $(PREFIX)/usr/share/susemanager
	$(INSTALL_DATA) $(SUMA_INSTALL_DATA_FILES) $(PREFIX)/usr/share/susemanager
