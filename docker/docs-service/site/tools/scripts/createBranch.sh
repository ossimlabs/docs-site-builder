#!/bin/bash
#Script to make bundles 1=branchname

cd libraries
git checkout master
git pull
git checkout -b ${1}
git push --set-upstream origin ${1}

cd ../common
git checkout master
git pull
git checkout -b ${1}
git push --set-upstream origin ${1}

cd ../plugins
git checkout master
git pull
git checkout -b ${1}
git push --set-upstream origin ${1}

cd ../managers
git checkout master
git pull
git checkout -b ${1}
git push --set-upstream origin ${1}

cd ../webservices
git checkout master
git pull
git checkout -b ${1}
git push --set-upstream origin ${1}

cd ../webapps
git checkout master
git pull
git checkout -b ${1}
git push --set-upstream origin ${1}