SHELL=/bin/bash
JAVAC=javac
OUTPUT_DIR=executables

# yeah I know this is terrible, but it is the lowest cost solution for a tiny project
SRC=$(shell find . | grep .java$)
OBJS=$(shell find . | grep .java$ | sed -e 's/^.\/src/.\/bin/g' | sed -e 's/.java$$/.class/g')

LIBS=$(shell find lib | grep .jar$)

$(OBJS): %.class : %.java
	$(JAVAC) -classpath $(LIBS) $<

$(OUTPUT_DIR)/prosevis.jar: ${OBJS}
	jar cmf scripts/manifest $(OUTPUT_DIR)/prosevis.jar -C ./bin

mac_bundle: $(OUTPUT_DIR)/prosevis.jar


clean:
	rm -rf bin
	rm -rf executables

