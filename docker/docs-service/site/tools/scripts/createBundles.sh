#!/bin/bash
#Script to make bundles 1=curdate 2=numDays 3=branch
#Run from root of projects directory 

echo Creating bundles for ${1} for ${2} days
echo
echo
echo Libraries Bundle
echo
cd libraries
git checkout master
git pull
git checkout ${3}
git pull
git checkout master
git tag -f libraries${1} master
git push origin libraries${1}
git bundle create libraries.bundle --since=${2}.days --branches --tags
echo
echo
echo Common Bundle
echo
cd ../common
git checkout master
git pull
git checkout ${3}
git pull
git checkout master
git tag -f common${1} master
git push origin common${1}
git bundle create common.bundle --since=${2}.days --branches --tags
echo
echo
echo Managers Bundle
echo
cd ../managers
git checkout master
git pull
git checkout ${3}
git pull
git checkout master
git tag -f managers${1} master
git push origin managers${1}
git bundle create managers.bundle --since=${2}.days --branches --tags
echo
echo
echo Webapps Bundle
echo
cd ../webapps
git checkout master
git pull
git checkout ${3}
git pull
git checkout master
git tag -f webapps${1} master
git push origin webapps${1}
git bundle create webapps.bundle --since=${2}.days --branches --tags
echo
echo
echo Webservices Bundle
echo
cd ../webservices
git checkout master
git pull
git checkout ${3}
git pull
git checkout master
git tag -f webservices${1} master
git push origin webservices${1}
git bundle create webservices.bundle --since=${2}.days --branches --tags
echo
echo
echo Plugins Bundle
echo
cd ../plugins
git checkout master
git pull
git checkout ${3}
git pull
git checkout master
git tag -f plugins${1} master
git push origin plugins${1}
git bundle create plugins.bundle --since=${2}.days --branches --tags
echo
echo
echo Repo Bundle
echo
cd ../repo
git checkout master
git pull
git tag -f repo${1} master
git push origin repo${1}
git bundle create repo.bundle --since=${2}.days --branches --tags
echo
echo
echo Tools Bundle
echo
cd ../tools
git checkout master
git pull
git tag -f tools${1} master
git push origin tools${1}
git bundle create tools.bundle --since=${2}.days --branches --tags
echo
echo
echo Build Bundle
echo
cd ../build
git checkout master
git pull
git tag -f build${1} master
git push origin build${1}
git bundle create build.bundle --since=${2}.days --branches --tags
echo
echo
echo RB-Analytics Bundle
echo
cd ../rb-analytics
git checkout master
git pull
git tag -f rb-analytics${1} master
git push origin rb-analytics${1}
git bundle create rb-analytics.bundle --since=${2}.days --branches --tags