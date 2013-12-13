#!/usr/bin/env bash

# install the connect app
apt-get update
apt-get install openjdk-7-jdk curl git tmux vim -y

[[ ! -e lein ]] && wget https://raw.github.com/technomancy/leiningen/stable/bin/lein -O /usr/bin/lein
chmod +x /usr/bin/lein

[[ ! -e /var/lib/floraconnect ]] && mkdir -p /var/lib/floraconnect
chown vagrant /var/lib/floraconnect -Rf

