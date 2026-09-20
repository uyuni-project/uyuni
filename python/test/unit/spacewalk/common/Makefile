# SPDX-FileCopyrightText: Red Hat, Inc
# SPDX-FileCopyrightText: SUSE LLC
#
# SPDX-License-Identifier: GPL-2.0-only

TESTS       = \
        test_gettext.py \
        test_rhnCache.py

all:	$(addprefix test-,$(TESTS))

test-%:
	@echo Running $*
	@PYTHONPATH=$(PYTHONPATH):../../.. $(PYTHON_BIN) $*
