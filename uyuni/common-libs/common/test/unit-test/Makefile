TESTS       = test_rhnLib.py

all:	$(addprefix test-,$(TESTS))

test-%:
	@echo Running $*
	@PYTHONPATH=$(PYTHONPATH):../../.. $(PYTHON_BIN) $*
