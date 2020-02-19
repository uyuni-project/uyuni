
SQL_FILES  := $(shell ls *.sql)
POSTGRESQL_FILES  := $(addsuffix .postgresql,$(SQL_FILES))

all : $(POSTGRESQL_FILES)

%.sql.postgresql : %.sql
	ln $^ $@
