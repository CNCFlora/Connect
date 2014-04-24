#!/usr/bin/env bash

# install java and utils
apt-get update
apt-get install openjdk-7-jdk curl git tmux vim htop -y

# install leiningen
[[ ! -e lein ]] && wget https://raw.github.com/technomancy/leiningen/stable/bin/lein -O /usr/bin/lein
chmod +x /usr/bin/lein
su vagrant -c 'lein self-install'

# create db dir
[[ ! -e /var/lib/floraconnect ]] && mkdir -p /var/lib/floraconnect
chown vagrant /var/lib/floraconnect -Rf

# docker, to build the image
apt-key adv --keyserver hkp://keyserver.ubuntu.com:80 --recv-keys 36A1D7869245C8950F966E92D8576A8BA88D21E9
echo 'deb http://get.docker.io/ubuntu docker main' > /etc/apt/sources.list.d/docker.list
apt-get update
apt-get install lxc-docker

