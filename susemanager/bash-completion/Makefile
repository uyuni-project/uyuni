BUILDDIR=$(CURDIR)/build
INSTALL_DIR=$(DESTDIR)/usr/share/bash-completion/completions/

TARGETS=mgr-create-bootstrap-repo mgr-sync spacewalk-common-channels \
	spacewalk-remove-channel spacewalk-repo-sync

all: $(TARGETS)

$(TARGETS): %: completions/_common.bash completions/%.bash | $(BUILDDIR)
	cat $^ > $(BUILDDIR)/$@

$(BUILDDIR):
	mkdir $(BUILDDIR)

install:
	install -d $(INSTALL_DIR)
	install -m 644 $(addprefix $(BUILDDIR)/,$(TARGETS)) $(INSTALL_DIR)
clean:
	rm -r $(BUILDDIR)
