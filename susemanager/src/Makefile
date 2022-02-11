# Makefile for src
#

# what is the backend top dir
TOP     = ..

# Specific stuff
SUBDIR		= susemanager

SPACEWALK_FILES		= __init__ \
			mgr_clean_old_patchnames \
			mgr_delete_patch \
			authenticator \
			helpers \
			errata_helper package_helper\
			mgr_sync/__init__ \
			mgr_sync/cli \
			mgr_sync/channel \
			mgr_sync/config \
			mgr_sync/mgr_sync \
			mgr_sync/product \
			mgr_sync/version \
			mgr_sync/logger
SCRIPTS  = mgr-clean-old-patchnames \
	   mgr-create-bootstrap-repo mgr-delete-patch mgr-sync
SALT_SSH_SCRIPT = mgr-salt-ssh

SUMA_DATA_FILES = mgr_bootstrap_data

PYSUMA_DATA_FILES  = $(addsuffix .py,$(SUMA_DATA_FILES))
ifeq ($(PYTHON_BIN), python3)
PYCSUMA_DATA_FILES = $(addsuffix *,$(addprefix __pycache__/,$(SUMA_DATA_FILES)))
else
PYCSUMA_DATA_FILES = $(addsuffix .pyc,$(SUMA_DATA_FILES)) $(addsuffix .pyo,$(SUMA_DATA_FILES))
endif

SUMA_INSTALL_DATA_FILES += $(PYSUMA_DATA_FILES)


# check if we can build man pages
DOCBOOK = $(wildcard /usr/bin/docbook2man)

SGMLS	= $(wildcard *.sgml)
MANS	= $(patsubst %.sgml,%.8,$(SGMLS))

BINDIR	= /usr/bin
SBINDIR	 = /usr/sbin
MANDIR	?= /usr/man

EXTRA_DIRS = $(MANDIR)/man8 $(BINDIR) $(SBINDIR)

include $(TOP)/Makefile.defs

# install scripts
all :: $(SCRIPTS)
install :: $(SCRIPTS) $(PREFIX)/$(SBINDIR) $(SUMA_INSTALL_DATA_FILES)
	$(INSTALL_BIN) $(SCRIPTS) $(PREFIX)/$(SBINDIR)
	$(INSTALL_BIN) $(SALT_SSH_SCRIPT) $(PREFIX)/$(BINDIR)
	$(INSTALL_DIR) $(PREFIX)/usr/share/susemanager
ifeq ($(PYTHON_BIN), python3)
	$(INSTALL_DIR) $(PREFIX)/usr/share/susemanager/__pycache__
endif
	@$(foreach f,$(wildcard $(PYCSUMA_DATA_FILES)), \
		$(INSTALL_DATA) $(f) $(PREFIX)/usr/share/susemanager/$(f) ; )
	$(INSTALL_DATA) $(SUMA_INSTALL_DATA_FILES) $(PREFIX)/usr/share/susemanager

ifneq ($(DOCBOOK),)
# install man pages
all	:: $(MANS)
install :: $(MANS) $(PREFIX)/$(MANDIR)
	$(INSTALL_DATA) $(MANS) $(PREFIX)/$(MANDIR)/man8
endif

%.8 : %.sgml
	$(DOCBOOK) $<

clean ::
	@rm -fv $(MANS) manpage.* *.new

