#!/bin/bash

[[ ! $CONTEXT ]] && CONTEXT="/"
[[ ! $PROXY ]] && PROXY=""
cd /root 
PROXY=$PROXY CONTEXT=$CONTEXT java -server -jar jetty.jar --path $CONTEXT connect.war

