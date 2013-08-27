include ../common/common.mk

.PHONY: all
all: pgsql*

vanilla_1_7:
	@echo Building vanilla container for testing python code of the 1.7 branch.
	$(call create_dockerfile, sles11_sp2_base)
	docker build -t manager_python_testing_1_7 .
	$(call remove_dockerfile)

vanilla_Head:
	@echo Building vanilla container for testing python code of the Head branch.
	$(call create_dockerfile, sles11_sp3_base)
	docker build -t manager_python_testing_Head .
	$(call remove_dockerfile)

pgsql_1_7:
	@echo Building container for testing python code of the 1.7 branch with the PostgreSQL database installed and configured.
	$(call create_dockerfile, sles11_sp2_pgsql)
	docker build -t manager_python_testing_pgsql_1_7 .
	$(call remove_dockerfile)

pgsql_Head:
	@echo Building container for testing python code of the Head branch with the PostgreSQL database installed and configured.
	$(call create_dockerfile, sles11_sp3_pgsql)
	docker build -t manager_python_testing_pgsql_Head .
	$(call remove_dockerfile)
