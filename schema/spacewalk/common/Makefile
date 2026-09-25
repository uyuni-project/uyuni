# SPDX-FileCopyrightText: 2026 Red Hat, Inc.
# SPDX-FileCopyrightText: 2026 SUSE LLC
#
# SPDX-License-Identifier: GPL-2.0-only

ifndef COMMON
COMMON := ../../../common
$(warning COMMON not defined, using $(COMMON))
endif
ifndef NAME
NAME := $(notdir $(shell cd ../; pwd))
endif

SRCDIR    := $(shell find $(COMMON) -name $(NAME))
SOURCES   := $(wildcard $(SRCDIR)/*)
FILES     := $(notdir $(SOURCES))
RMLINKS   := find . -type l -delete
RMTRASH   := rm -f lextab* parsetab* parser.out


all :
	$(MAKE) $(MAKEOVERRIDES) files

files : $(FILES)
	@$(RMTRASH)

.PHONY :
