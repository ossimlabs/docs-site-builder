ECHO Cloning git repos
PAUSE

ECHO BluegroundCommon
git clone ssh://git@10.0.0.17:7999/blueground/common.git

ECHO Documents
git clone ssh://git@10.0.0.17:7999/blueground/documents.git

ECHO BluegroundManagers
git clone ssh://git@10.0.0.17:7999/blueground/managers.git

ECHO BluegroundServices
git clone ssh://git@10.0.0.17:7999/blueground/webservices.git

ECHO Services
git clone ssh://git@10.0.0.17:7999/blueground/libraries.git

ECHO BluegroundWebapps
git clone ssh://git@10.0.0.17:7999/blueground/webapps.git

ECHO Tools
git clone ssh://git@10.0.0.17:7999/blueground/tools.git

ECHO rb-master
git clone ssh://git@10.0.0.17:7999/common/rbmaster.git

ECHO Install rb-master
cd rbmaster
mvn clean install

PAUSE