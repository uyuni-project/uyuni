include ../../common/common.mk

.PHONY: all
all: pgsql_Head

vanilla_Head:
	@echo Building vanilla container for testing java code of the Head branch.
	$(call create_dockerfile, sles11_sp3_base)
	docker build -t manager_java_testing_Head .
	$(call remove_dockerfile)

pgsql_Head:
	@echo Building container for testing java code of the Head branch with the PostgreSQL database installed and configured.
	$(call create_dockerfile, sles11_sp3_pgsql)
	docker build -t manager_java_testing_pgsql_Head .
	$(call remove_dockerfile)

