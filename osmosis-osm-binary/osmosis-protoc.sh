#!/bin/sh

protoc --java_out=gen-src/main/java src/main/protobuf/fileformat.proto src/main/protobuf/osmformat.proto
