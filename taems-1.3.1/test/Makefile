include ../Makefile.tmpl

JLIBS	= $(wildcard ../$(LIBS)/*[".jar"|".zip"])
CLASSPTH= $(CLASSD):$(subst $(SPACE),:,$(strip $(JLIBS)))

TESTFILES	= $(shell ls *.ttaems)
PTAEMSFILES	= $(shell ls *.ptaems)

%.ttaems:  FORCE
	@echo "Parsing test file $@"
	@$(JAVART)java -classpath $(CLASSPTH) taems.parser.ParserTest -silent -if $@ -of $@

%.ptaems:  FORCE
	@echo "Parsing test file $@"
	@NAME=`basename $@ .ptaems`; \
	 $(JAVART)java -classpath $(CLASSPTH) taems.preprocessor.PreProcessorParser < $@ > $$NAME.ttaems 2> /dev/null

testttaems:	$(TESTFILES) 
	@for i in $(TESTFILES); \
	do \
	  echo "Testing Version 1.0 for $$i"; \
	   diff $$i-1.0 results/$$i-1.0; \
	  echo "Testing Version 1.0A for $$i"; \
	   diff $$i-1.0A results/$$i-1.0A; \
	  echo "Testing Version 1.1 for $$i"; \
	   diff $$i-1.1 results/$$i-1.1; \
	  echo "If diff shows no errors everything is fine"; \
	  rm $$i-1.0 $$i-1.0A $$i-1.1; \
	done

testptaems:	$(PTAEMSFILES) 
	@for i in $(PTAEMSFILES); \
	do \
	  NAME=`basename $$i .ptaems`; \
	  echo "Testing PreTaems for $$i"; \
	   diff $$NAME.ttaems results/$$NAME.ttaems; \
	  echo "If diff shows no errors everything is fine"; \
	  rm $$NAME.ttaems; \
	done

updatetestttaems: $(TESTFILES)
	@for i in $(TESTFILES); \
	do \
	  echo "Updating Version 1.0 for $$i"; \
	   mv $$i-1.0 results; \
	  echo "Updating Version 1.0A for $$i"; \
	   mv $$i-1.0A results; \
	  echo "Updating Version 1.1 for $$i"; \
	   mv $$i-1.1 results; \
	done

updatetestptaems: $(PTAEMSFILES)
	@for i in $(PTAEMSFILES); \
	do \
	  NAME=`basename $$i .ptaems`; \
	  echo "Updating PreTaems for $$i"; \
	   mv $$NAME.ttaems results/$$NAME.ttaems; \
	done

updatetest: updatetestttaems

test: testttaems testptaems
