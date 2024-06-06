#!/bin/bash
#
# Phaedra II
#
# Copyright (C) 2016-2023 Open Analytics
#
# ===========================================================================
#
# This program is free software: you can redistribute it and/or modify
# it under the terms of the Apache License as published by
# The Apache Software Foundation, either version 2 of the License, or
# (at your option) any later version.
#
# This program is distributed in the hope that it will be useful,
# but WITHOUT ANY WARRANTY; without even the implied warranty of
# MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
# Apache License for more details.
#
# You should have received a copy of the Apache License
# along with this program.  If not, see <http://www.apache.org/licenses/>
#

# Enable the feature to stop script if any command fails
set -e

# fetch current version
current_version=`mvn -q -Dexec.executable=echo -Dexec.args='${project.version}' --non-recursive exec:exec`
echo "This is current_version: $current_version"

# Remove -SNAPSHOT from version
release_version=${current_version/-SNAPSHOT/}
echo "Release version is $release_version"

# Set release version
mvn versions:set -DnewVersion="$release_version"
# Update child module's to parent release version
mvn versions:update-child-modules
# Update parent to latest release version
mvn versions:update-parent -U

## Commit and push updated pom file
git add pom.xml
git add dto/pom.xml
git add java-stat-worker/pom.xml
git add r-worker/pom.xml
git add worker/pom.xml
git commit -m "update:d set version to $release_version"
git push

# Use got flow maven plugin to release the project
mvn -B -DskipTestProject=true gitflow:release-start gitflow:release-finish

# Update parent to latest snapshot version
mvn versions:update-parent -DallowSnapshots=true -U
mvn versions:update-child-modules -DallowSnapshots=true

# Clean up, remove pom.xml.versionsBackup files created by mvn versions:set
find . -name "pom.xml.versionsBackup" -type f | xargs rm
