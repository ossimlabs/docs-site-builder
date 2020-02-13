#/bin/bash
cd libraries
git checkout master
git pull
git tag sprint15.15
git push origin sprint15.15

cd ../common
git checkout master
git pull
git tag sprint15.15
git push origin sprint15.15

cd ../managers
git checkout master
git pull
git tag sprint15.15
git push origin sprint15.15

cd ../plugins
git checkout master
git pull
git tag sprint15.15
git push origin sprint15.15

cd ../webapps
git checkout master
git pull
git tag sprint15.15
git push origin sprint15.15

cd ../webservices
git checkout master
git pull
git tag sprint15.15
git push origin sprint15.15

cd ../rb-analytics
git checkout master
git pull
git tag sprint15.15
git push origin sprint15.15