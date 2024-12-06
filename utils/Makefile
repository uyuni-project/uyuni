# Makefile for spacewalk-utils
#

NAME := spacewalk-utils

SUBDIR  = utils

FILES	= __init__ depsolver cloneByDate
SCRIPTS = spacewalk-common-channels spacewalk-hostname-rename \
	  spacewalk-clone-by-date spacewalk-sync-setup spacewalk-manage-channel-lifecycle \
	  taskotop spacewalk-watch-channel-sync.sh
CONFIGS = spacewalk-common-channels.ini

DOCBOOK = $(wildcard /usr/bin/docbook2man)

SGMLS	= $(wildcard *.sgml)
MANS	= $(patsubst %.sgml,%.8,$(SGMLS) )

BINDIR	= /usr/bin
MANDIR	?= /usr/man
CONFDIR = /etc/rhn

EXTRA_DIRS = $(MANDIR)/man8 $(BINDIR) $(CONFDIR)

include Makefile.defs

# install scripts
all :: $(SCRIPTS)
install :: $(SCRIPTS) $(PREFIX)/$(BINDIR)
	$(INSTALL_BIN) $(SCRIPTS) $(PREFIX)/$(BINDIR)

ifneq ($(DOCBOOK),)
# install man pages
all	:: $(MANS)

install :: $(MANS) $(PREFIX)/$(MANDIR)
	$(INSTALL_DATA) $(MANS) $(PREFIX)/$(MANDIR)/man8
install :: $(CONFIGS) $(PREFIX)/$(CONFDIR)
	$(INSTALL_DATA) $(CONFIGS) $(PREFIX)/$(CONFDIR)
endif

%.new : %
	sed -e 's|@@ROOT@@|$(ROOT)|g' <$* >$@

%.8 : %.sgml
	$(DOCBOOK) $<

clean ::
	@rm -fv $(MANS) manpage.* *.new

