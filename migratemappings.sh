#!/usr/bin/env bash
rm -rf ./remappedSrc/ && ./gradlew migrateMappings --mappings "$1"
