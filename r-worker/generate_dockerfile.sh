#!/usr/bin/env bash

curl https://raw.githubusercontent.com/docker-library/openjdk/master/18/jdk/buster/Dockerfile -o java.Dockerfile

sed -i "s/^FROM.*//g" java.Dockerfile
sed -i "s/^CMD.*//g" java.Dockerfile
sed -i "s/^#.*//g" java.Dockerfile
sed -i '/^$/d' java.Dockerfile

sed -e '/#<<<INSTALL_JAVA>>>#/{r java.Dockerfile' -e 'd}' template.Dockerfile > Dockerfile
sed -i "s/# Template file: can be changed/# Generated file: do NOT change/g" Dockerfile

rm java.Dockerfile
