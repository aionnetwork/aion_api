#!/usr/bin/env bash
protoc -I=../src/resource --java_out=../src ../src/resource/message.proto
