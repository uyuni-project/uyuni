include ../../rel-eng/Makefile

TOP      := .
SPECFILE := $(firstword $(wildcard *.spec))
NAME     := $(shell rpm -q --qf '%{name}\n' --specfile $(SPECFILE) | head -n1)
VERSION  := $(shell rpm -q --qf '%{version}\n' --specfile $(SPECFILE) | head -n1)
RELEASE  := $(shell rpm -q --qf '%{release}\n' --specfile $(SPECFILE) | head -n1)
NEXTVERSION := $(shell echo $(VERSION) | awk '{ pre=post=$$0; gsub("[0-9]+$$","",pre); gsub(".*\\.","",post); print pre post+1; }')

spacewalk-clean satellite-clean : FORCE
	$(MAKE) -f $(TOP)/Makefile.schema clean

spacewalk-release satellite-release : FORCE
	$(MAKE) -f $(TOP)/Makefile.schema SCHEMA=$(NAME) VERSION=$(VERSION) RELEASE=$(RELEASE) all

FORCE :

newmigration:
	@mkdir -p upgrade/$(NAME)-$(VERSION)-to-$(NAME)-$(NEXTVERSION)
	@if [ -e upgrade/$(NAME)-$(VERSION)-to-$(NAME)-$(NEXTVERSION)/new.sql ]; then \
		echo "Please rename first upgrade/$(NAME)-$(VERSION)-to-$(NAME)-$(NEXTVERSION)/new.sql"; \
		exit 1; \
	fi
	@touch upgrade/$(NAME)-$(VERSION)-to-$(NAME)-$(NEXTVERSION)/new.sql
	@echo "New migration file at upgrade/$(NAME)-$(VERSION)-to-$(NAME)-$(NEXTVERSION)/new.sql"
