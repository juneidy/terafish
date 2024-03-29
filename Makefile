# Program packages and files
#   - The packages should be the path inside your src directory.
#   eg: package1 package2/package3
PACKAGES = 

# Java compiler
JAVAC = javac

# Directory for compiled binaries
# - trailing slash is important!
BIN = ./build/

# Main class
#MAIN = ImageProcess
MAIN = TeraFish

# Executable file
EXEC = $(MAIN)

# Create the bin directory if it does not exist
MKDIR = mkdir -p

# Directory of source files
# - trailing slash is important!
SRC = ./src/

# Java compiler flags
JAVAFLAGS = -g -d $(BIN) -cp $(SRC)

# Creating a .class file
COMPILE = $(JAVAC) $(JAVAFLAGS)

EMPTY = 

JAVA_FILES = $(subst $(SRC), $(EMPTY), $(wildcard $(SRC)*.java))

ifdef PACKAGES
PACKAGEDIRS = $(addprefix $(SRC), $(PACKAGES))
PACKAGEFILES = $(subst $(SRC), $(EMPTY), $(foreach DIR, $(PACKAGEDIRS), \
					$(wildcard $(DIR)/*.java)))
ALL_FILES = $(PACKAGEFILES) $(JAVA_FILES)
else
#ALL_FILES = $(wildcard $(SRC).java)
ALL_FILES = $(JAVA_FILES)
endif

# One of these should be the "main" class listed in Runfile
# CLASS_FILES = $(subst $(SRC), $(BIN), $(ALL_FILES:.java=.class))
CLASS_FILES = $(ALL_FILES:.java=.class)

# The first target is the one that is executed when you invoke
# "make". 

build: directory $(addprefix $(BIN), $(CLASS_FILES))

directory:
	$(MKDIR) $(BIN)

# The line describing the action starts with <TAB>
$(BIN)%.class : $(SRC)%.java
	$(COMPILE) $<

package : TeraFish.jar

TeraFish.jar: build $(addprefix $(BIN), $(CLASS_FILES))
	jar cvfm TeraFish.jar Manifest.txt -C build . templates

clean : 
	rm -rf build TeraFish.jar
