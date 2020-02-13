#/bin/bash
cd libraries
git checkout master
git pull
git checkout -b release/asm_1.0.15
git push -u origin release/asm_1.0.15
git checkout master

cd ../common
git checkout master
git pull
git checkout -b release/asm_1.0.15
git push -u origin release/asm_1.0.15
git checkout master

cd ../managers
git checkout master
git pull
git checkout -b release/asm_1.0.15
git push -u origin release/asm_1.0.15
git checkout master

cd ../plugins
git checkout master
git pull
git checkout -b release/asm_1.0.15
git push -u origin release/asm_1.0.15
git checkout master

cd ../webapps
git checkout master
git pull
git checkout -b release/asm_1.0.15
git push -u origin release/asm_1.0.15
git checkout master

cd ../webservices
git checkout master
git pull
git checkout -b release/asm_1.0.15
git push -u origin release/asm_1.0.15
git checkout master

cd ../rb-analytics
git checkout master
git pull
git checkout -b release/asm_1.0.15
git push -u origin release/asm_1.0.15
git checkout master