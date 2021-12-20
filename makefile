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

default:
	@make $(JAR)
	@mkdir -p $(DIR_A)
	@mkdir -p $(DIR_B)
	@rm $(DIR_A)/*
	@rm $(DIR_B)/*
	@cp $(SRC_DIR)/utils/ProtocolDescription.txt $(DIR_A)
	@cp $(JAR) $(TEST_PATH)
	@echo "hello world" > $(DIR_B)/helloworld.txt


$(JAR): $(SRC_FILES)
	@mvn package