#!/usr/bin/env bash

# update
apt-get update

# install java
apt-get install -y openjdk-7-jdk

# install maven
apt-get install -y maven

# install git
apt-get install -y git

# install gradle
apt-get install -y gradle

# nodejs install
apt-get install -y curl
curl -sL https://deb.nodesource.com/setup_4.x | sudo -E bash -
apt-get install -y nodejs
