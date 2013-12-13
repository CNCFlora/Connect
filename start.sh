#!/bin/bash

APP_USER=cncflora
su $APP_USER -c "cd ~/ && nohup java -jar connect.jar  > connect.log &"

/usr/sbin/sshd -D

