#!/usr/bin/env bash

# A script to deploy all of the microservices to openshift

# microservice-authz
cd microservice-authz
mvn clean package
docker build -t example/microservice-authz .
oc create -f src/main/kubernetes/Deployment.yml
oc create -f src/main/kubernetes/Service.yml
oc expose service microservice-authz
cd ..

# microservice-session
cd microservice-session
mvn clean package
docker build -t example/microservice-session .
oc create -f src/main/kubernetes/Deployment.yml
oc create -f src/main/kubernetes/Service.yml
oc expose service microservice-session
cd ..

# microservice-schedule
cd microservice-schedule
mvn clean package
docker build -t example/microservice-schedule .
oc create -f src/main/kubernetes/Deployment.yml
oc create -f src/main/kubernetes/Service.yml
oc expose service microservice-schedule
cd ..

# microservice-speaker
cd microservice-speaker
mvn clean package
docker build -t example/microservice-speaker .
oc create -f src/main/kubernetes/Deployment.yml
oc create -f src/main/kubernetes/Service.yml
oc expose service microservice-speaker
cd ..

# microservice-vote
cd microservice-vote
mvn clean package
docker build -t example/microservice-vote .
oc create -f src/main/kubernetes/Deployment.yml
oc create -f src/main/kubernetes/Service.yml
oc expose service microservice-vote
cd ..
