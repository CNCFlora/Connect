#!/usr/bin/env bash

# install java and utils
apt-get update && apt-get upgrade
apt-get install openjdk-7-jdk curl git tmux vim htop -y

# install leiningen
[[ ! -e lein ]] && wget https://raw.github.com/technomancy/leiningen/stable/bin/lein -O /usr/bin/lein
chmod +x /usr/bin/lein
su vagrant -c 'lein self-install'

# create db dir
[[ ! -e /var/lib/floraconnect ]] && mkdir -p /var/lib/floraconnect
chown vagrant /var/lib/floraconnect -Rf

