#!/bin/bash

minishift profile set mp12-demo
minishift config set memory 4GB
minishift config set cpus 3
minishift config set vm-driver virtualbox
minishift config set image-caching true
minishift addon enable admin-user
minishift config set openshift-version v3.7.1

minishift start
