#!/usr/bin/env bash

# install java and utils
apt-get update
apt-get install openjdk-7-jdk curl git tmux vim htop -y

# install leiningen
[[ ! -e lein ]] && wget https://raw.github.com/technomancy/leiningen/stable/bin/lein -O /usr/bin/lein
chmod +x /usr/bin/lein

# create db dir
[[ ! -e /var/lib/floraconnect ]] && mkdir -p /var/lib/floraconnect
chown vagrant /var/lib/floraconnect -Rf

# prepare startup
echo 'echo $(date) > /var/log/rc.log' > /etc/rc.log
echo 'su -c "cd /vagrant && nohup lein ring server-headless &" >> /var/log/rc.log 2>&1' >> /etc/rc.local
echo 'SUBSYSTEM=="bdi",ACTION=="add",RUN+="/vagrant/register.sh >> /var/log/rc.log 2>&1"' > /etc/udev/rules.d/50-vagrant.rules

# start the service and register it
cd /vagrant && su vagrant -c 'nohup lein ring server-headless &'
cd /vagrant && ./register.sh
