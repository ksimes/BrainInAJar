####### Location of JNI include files
JDK_HOME = /usr/java/jdk

IDIRS = -I$(JDK_HOME)/include -I$(JDK_HOME)/include/linux
#########

BUILD = ./build

JAVA = $(JDK_HOME)/bin/java
JAVAC = $(JDK_HOME)/bin/javac
JAVAH = $(JDK_HOME)/bin/javah
JAVADOC = $(JDK_HOME)/bin/javadoc

SRC = src
PACK = $(SRC)/org/lirc
JLIRC = $(PACK)/*.java
SOCKET = $(PACK)/socket/*.java
UTIL = $(PACK)/util/*.java
UI = $(PACK)/ui/*.java
TEST = $(PACK)/test/*.java
ALL = $(JLIRC) $(SOCKET) $(UTIL) $(UI) $(TEST)

ALLPACK = org.lirc org.lirc.socket org.lirc.test org.lirc.util org.lirc.ui

CC = gcc
C_FILE = $(PACK)/socket/junixsocket.c
H_FILE = $(BUILD)/junixsocket.h
SO_FILE = $(BUILD)/org/lirc/socket/libjunixsocket.so
JAVAH_CLASSES = org.lirc.socket.UnixSocketImpl org.lirc.socket.UnixSocketInputStream org.lirc.socket.UnixSocketOutputStream

APIDIR = apidoc

TODAY = `date +%Y%m%d`

default all: classes jni

classes:
	mkdir -p $(BUILD)
	$(JAVAC) -d $(BUILD) -sourcepath $(SRC) $(ALL)

jni:
	mkdir -p $(BUILD)
	$(JAVAH) -classpath $(BUILD) -jni -o $(H_FILE) -jni $(JAVAH_CLASSES)
	$(CC) -march=i386 -shared -fpic $(IDIRS) -I$(BUILD) -o $(SO_FILE) $(C_FILE)

dists: srcdist userdist docdist

srcdist: distclean
	cd .. && find jlirc/ ! -path '*CVS*' | zip jlirc/jlirc-src-$(TODAY).zip -@

userdist: all
	mkdir -p $(BUILD)/jlirc
	cp README.user INSTALL.user LICENSE $(BUILD)/jlirc
	cp -r $(BUILD)/org $(BUILD)/jlirc
	cd $(BUILD) && zip -r ../jlirc-user-$(TODAY).zip jlirc

docdist: doc
	cd .. && zip -r jlirc/jlirc-doc-$(TODAY).zip jlirc/apidoc

doc:
	mkdir -p $(APIDIR)
	$(JAVADOC) -version -author -use -overview $(PACK)/overview.html -d $(APIDIR) -sourcepath $(SRC) $(ALLPACK)

docclean:
	-rm -rf $(APIDIR)

clean:
	-rm -rf $(BUILD) *.jar *.zip

distclean: docclean clean

test:
	$(JAVA) -cp $(BUILD) org.lirc.test.Irw -ins
