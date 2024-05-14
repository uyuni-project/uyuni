# Makefile for spacewalk backend
#

# what is the backend top dir
TOP     = ..

# Specific stuff
SUBDIR  = common
SPACEWALK_FILES	=   __init__ \
		checksum \
		cli \
		context_managers \
		fileutils \
		rhnLib \
		rhn_deb \
		rhn_mpm \
		rhn_pkg \
		rhn_rpm \
		timezone_utils \
		usix

SCRIPTS =

# check if we can build man pages
DOCBOOK = $(wildcard /usr/bin/docbook2man)

SGMLS   = $(wildcard *.sgml)
MANS    = $(patsubst %.sgml,%.8,$(SGMLS))

MANDIR  ?= /usr/man

EXTRA_DIRS =

include $(TOP)/Makefile.defs

# install scripts

ifneq ($(DOCBOOK),)
# install man pages
all     :: $(MANS)
install :: $(MANS) $(PREFIX)/$(MANDIR)
	$(INSTALL_DATA) $(MANS) $(PREFIX)/$(MANDIR)/man8
endif

%.8 : %.sgml
	$(DOCBOOK) $<

clean :: 
	@rm -fv $(MANS) manpage.*

