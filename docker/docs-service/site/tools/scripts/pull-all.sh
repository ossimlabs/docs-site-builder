#!/bin/bash

branch=$1
if [ -z $branch ]; then
	echo "Not Changing Branches"
else
	echo "using branch $branch"

fi


function pull-repo
{
	echo "Pulling $1"
	cd $1
	#git clean -df
	#git reset --hard
	if [ -z $branch ]; then
		echo "Not Changing Branches"
	else
		git checkout $branch
	fi
	git fetch -p
	git pull
	cd ..
}

pull-repo common
pull-repo documents
pull-repo libraries
pull-repo managers
pull-repo plugins
pull-repo tools
pull-repo webapps
pull-repo webservices
pull-repo rb-analytics
pull-repo repo
pull-repo dependencies-internal
pull-repo .
