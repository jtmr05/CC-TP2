.DEFAULT_GOAL := default
.PHONY: default jar

TEST_PATH := $${HOME}/Documents/testDir
DIR_A := $(TEST_PATH)/src
DIR_B := $(TEST_PATH)/dst
JAR := target/FFSync.jar

SRC_DIR := src
SRC_FILES := $(shell find $(SRC_DIR) -name *.java) $(shell find $(SRC_DIR) -name *rotocol*)


jar:
	@make $(JAR)

$(JAR): $(SRC_FILES)
	@mvn package
