#!/bin/bash
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
