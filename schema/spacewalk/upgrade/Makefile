# SPDX-FileCopyrightText: 2026 SUSE LLC
#
# SPDX-License-Identifier: GPL-2.0-only

SQL_FILES  := $(shell ls *.sql)
POSTGRESQL_FILES  := $(addsuffix .postgresql,$(SQL_FILES))

all : $(POSTGRESQL_FILES)

%.sql.postgresql : %.sql
	ln $^ $@
