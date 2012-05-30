SHELL=/bin/bash
JAVAC=javac
OUTPUT_DIR=executables

# Paths and constants for the MacOS bundle
BUNDLE_PATH=$(OUTPUT_DIR)/ProseVis.app
BUNDLE_JAVA_ROOT=$(BUNDLE_PATH)/Contents/Resources/Java
BUNDLE_EXEC_DST=$(BUNDLE_PATH)/Contents/MacOS/JavaApplicationStub
BUNDLE_EXEC_SRC=/System/Library/Frameworks/JavaVM.framework/Resources/MacOS/JavaApplicationStub

# zip is retarded and needs me to copy everything into a single directory before I zip
WIN32_PATH=$(OUTPUT_DIR)/win32/
WIN64_PATH=$(OUTPUT_DIR)/win64/


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

# Pipe indicates that OUTPUT_DIR is an order only dependency
# I believe this means it ignores timestamp
$(OUTPUT_DIR)/prosevis.jar: $(OBJ) | $(OUTPUT_DIR)
	@echo Building prosevis.jar
	jar cmf resources/manifest $(OUTPUT_DIR)/prosevis.jar -C ./src prosevis

$(OUTPUT_DIR):
	mkdir $(OUTPUT_DIR)


mac_package: $(OUTPUT_DIR)/prosevis.jar
	@echo Building MacOSX bundle
	rm -rf   $(BUNDLE_PATH)
	mkdir -p $(BUNDLE_PATH)
	mkdir -p $(BUNDLE_PATH)/Contents/MacOS
	mkdir -p $(BUNDLE_JAVA_ROOT)
	cp resources/prosevisicon.icns executables/ProseVis.app/Contents/Resources/
	cp $(OUTPUT_DIR)/prosevis.jar $(BUNDLE_JAVA_ROOT)/
	cp lib/processingopengl/macosx/*.jnilib $(BUNDLE_JAVA_ROOT)/
	cp lib/processingopengl/*.jar $(BUNDLE_JAVA_ROOT)/
	cp lib/*.jar $(BUNDLE_JAVA_ROOT)/
	cp resources/JavaApplicationStub $(BUNDLE_PATH)/Contents/MacOS
	cp resources/PkgInfo $(BUNDLE_PATH)/Contents/
	cp resources/Info.plist $(BUNDLE_PATH)/Contents/
	./scripts/update_info_plist.sh $(BUNDLE_PATH)/Contents/Info.plist
	pushd $(OUTPUT_DIR) && tar czvf prosevis.mac.tar.gz ProseVis.app

win32_package: $(OUTPUT_DIR)/prosevis.jar
	rm -rf $(WIN32_PATH)
	mkdir -p $(WIN32_PATH)
	cp $(OUTPUT_DIR)/prosevis.jar $(WIN32_PATH)
	cp lib/processingopengl/*.jar $(WIN32_PATH)
	cp lib/*.jar $(WIN32_PATH)
	cp lib/processingopengl/windows32/*.dll $(WIN32_PATH)
	cp resources/run32.bat $(WIN32_PATH)/run.bat
	pushd $(WIN32_PATH) && find . | xargs zip prosevis.win32.zip && popd
	mv $(WIN32_PATH)/prosevis.win32.zip $(OUTPUT_DIR)

win64_package: $(OUTPUT_DIR)/prosevis.jar
	rm -rf $(WIN64_PATH)
	mkdir -p $(WIN64_PATH)
	cp $(OUTPUT_DIR)/prosevis.jar $(WIN64_PATH)
	cp lib/processingopengl/*.jar $(WIN64_PATH)
	cp lib/*.jar $(WIN64_PATH)
	cp lib/processingopengl/windows64/*.dll $(WIN64_PATH)
	cp resources/run64.bat $(WIN64_PATH)/run.bat
	pushd $(WIN64_PATH) && find . | xargs zip prosevis.win64.zip && popd
	mv $(WIN64_PATH)/prosevis.win64.zip $(OUTPUT_DIR)

linux32_package: $(OUTPUT_DIR)/prosevis.jar
	tar czvf $(OUTPUT_DIR)/prosevis.linux32.tar.gz \
		-C $(OUTPUT_DIR) prosevis.jar \
		-C ../lib $(shell ls lib | grep .jar$$) \
		-C ../lib/processingopengl $(shell ls lib/processingopengl | grep .jar$$) \
		-C linux32 $(shell ls lib/processingopengl/linux32/ | grep .so$$) \
		-C ../../../resources run.sh 

linux64_package: $(OUTPUT_DIR)/prosevis.jar
	tar czvf $(OUTPUT_DIR)/prosevis.linux64.tar.gz \
		-C $(OUTPUT_DIR) prosevis.jar \
		-C ../lib $(shell ls lib | grep .jar$$) \
		-C ../lib/processingopengl $(shell ls lib/processingopengl | grep .jar$$) \
		-C linux64 $(shell ls lib/processingopengl/linux64/ | grep .so$$) \
		-C ../../../resources run.sh 

data_package:
	cd data && zip ../$(OUTPUT_DIR)/data.zip colorschemes/* shakespeare/*



all_packages: mac_package win32_package win64_package linux64_package linux32_package data_package

.PHONY : clean 


clean:
	rm -f $(OBJ)
	rm -rf $(OUTPUT_DIR)

