#!/bin/bash

[[ ! $HOST ]] && HOST="$(hostname -I | awk '{print $1}')"
[[ ! $PORT ]] && PORT=8585
[[ ! $APP ]]  && APP="connect"
[[ ! $ETCD ]] && ETCD="http://${HOST}:4001"

OPTS="-e HOST=$HOST -e APP=$APP -e PORT=$PORT -e ETCD=$ETCD";

docker run -d -p $PORT:8080 -p 2525:22 $OPTS -name $APP cncflora/$APP /root/start.sh

