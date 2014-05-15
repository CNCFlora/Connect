#!/usr/bin/env bash

# install java
apt-get update && apt-get upgrade -y
apt-get install openjdk-7-jdk wget -y

# install leiningen
if [[ ! -e /usr/bin/lein ]] ; then
    wget https://raw.github.com/technomancy/leiningen/stable/bin/lein -O /usr/bin/lein
    chmod +x /usr/bin/lein
    su vagrant -c 'lein self-install'
fi

# create db dir
if [[ ! -e /var/lib/floraconnect ]] ; then 
    mkdir -p /var/lib/floraconnect
    chown vagrant /var/lib/floraconnect -Rf
fi

