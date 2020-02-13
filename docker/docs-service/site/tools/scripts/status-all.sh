#!/bin/bash
function git-status
{
	echo "***** $1 *****"
	echo "***** $1 *****"
	echo "***** $1 *****"
	echo "***** $1 *****"
	cd $1
	git status
	cd ..
}

git-status common
git-status documents
git-status libraries
git-status managers
git-status plugins
git-status tools
git-status webapps
git-status webservices
git-status rb-analytics
git-status repo
git-status dependencies-internal
git-status .
