#!/bin/bash

[[ ! $CONTEXT ]] && CONTEXT="/"
cd /root 
java -jar jetty.jar --path $CONTEXT connect.war

