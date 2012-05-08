SHELL=/bin/bash
JAVAC=javac
OUTPUT_DIR=executables

# Paths and constants for the MacOS bundle
BUNDLE_PATH=$(OUTPUT_DIR)/ProseVis.app
BUNDLE_JAVA_ROOT=$(BUNDLE_PATH)/Contents/Resources/Java
BUNDLE_EXEC_DST=$(BUNDLE_PATH)/Contents/MacOS/JavaApplicationStub
BUNDLE_EXEC_SRC=/System/Library/Frameworks/JavaVM.framework/Resources/MacOS/JavaApplicationStub

# yeah I know this is terrible, but it is the lowest cost solution for a tiny
# project where we use eclipse anyway
SRC=$(shell find . | grep .java$$)
OBJ=$(SRC:.java=.class)

LIBS := $(shell find lib | grep .jar$$)
# Leaves a final colon on JOINED_LIBS which we use to append the src dir at the end
JOINED_LIBS := $(shell find lib | grep .jar$$ | tr -s '\n' ':' )
COMPILE_CP := $(JOINED_LIBS)src

%.class : %.java
	$(JAVAC) -classpath $(COMPILE_CP) $<

all: $(OUTPUT_DIR)/prosevis.jar mac_bundle
	@echo Finished building all

$(OUTPUT_DIR):
	mkdir $(OUTPUT_DIR)

# Pipe indicates that OUTPUT_DIR is an order only dependency
# I believe this means it ignores timestamp
$(OUTPUT_DIR)/prosevis.jar: $(OBJ) | $(OUTPUT_DIR)
	@echo Building prosevis.jar
	jar cmf scripts/manifest $(OUTPUT_DIR)/prosevis.jar -C ./src prosevis

mac_bundle: $(OUTPUT_DIR)/prosevis.jar resources/Info.plist resources/prosevis.png $(LIBS)
	@echo Building MacOSX bundle
	rm -rf   $(BUNDLE_PATH)
	mkdir -p $(BUNDLE_PATH)
	mkdir -p $(BUNDLE_PATH)/Contents/MacOS
	mkdir -p $(BUNDLE_JAVA_ROOT)
	cp resources/prosevis.png executables/ProseVis.app/
	cp $(OUTPUT_DIR)/prosevis.jar $(BUNDLE_JAVA_ROOT)/
	cp lib/processingopengl/macosx/*.jnilib $(BUNDLE_JAVA_ROOT)/
	cp lib/processingopengl/*.jar $(BUNDLE_JAVA_ROOT)/
	cp resources/Info.plist $(BUNDLE_PATH)/Contents/
	./scripts/update_info_plist.sh $(BUNDLE_PATH)/Contents/Info.plist
	ln -s $(BUNDLE_EXEC_SRC) $(BUNDLE_EXEC_DST)

mac_package: mac_bundle
	tar czvf $(OUTPUT_DIR)/prosevis.mac.tar.gz $(BUNDLE_PATH) public_data
	

.PHONY : clean mac_bundle


clean:
	rm -f $(OBJ)
	rm -rf $(OUTPUT_DIR)

