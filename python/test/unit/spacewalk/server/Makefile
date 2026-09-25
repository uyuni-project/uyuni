# SPDX-FileCopyrightText: Red Hat, Inc
# SPDX-FileCopyrightText: SUSE LLC
#
# SPDX-License-Identifier: GPL-2.0-only

TESTS       = \
        test_rhnLib_timestamp.py

all:	$(addprefix test-,$(TESTS))

test-%:
	@echo Running $*
	PYTHONPATH=$(PYTHONPATH):../../../ $(PYTHON_BIN) $*
