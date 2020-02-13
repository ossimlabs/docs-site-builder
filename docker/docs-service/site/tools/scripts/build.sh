#!/bin/bash
# 1=branch 2=quiet 3=mvn properties

error() 
{
	echo "  **Arguments missing!**"
	echo
	echo "  1(required)=branch 2(required)=quiet(bool) 3(optional)=mvn properties"
	echo
	echo "  example: ./build.sh master true"
	echo "  example: ./build.sh master false -Dparam=true"
	exit
}

if [ $# -eq 0 ]
  then
    error
elif [ $# -eq 1 ]
  then
	error
fi

QUIET=""

if [ "${2}" = "true" ]; then
    QUIET="-q"
else
	QUIET=""
fi

# Install function
# 1=repo 2=branch 3=mvn command 4=mvn properties
install()
{
	echo
	echo "Building ${1}..."
	cd ${1}
	
	git checkout ${2} ${QUIET}
	git pull ${QUIET}
	mvn ${QUIET} ${3} ${4}
	
	status=$?
	
	if [ $status -eq 0 ]; then
		echo "done"
	else
		echo "Building ${1} failed. Aborting..."
		exit
	fi
	
	cd ..
}

# Clean the repo and target first
clean()
{
	echo "Cleaning repo and target..."
	cd repo
	git clean ${QUIET} -f -d
	cd ..
	
	if [ -d "target" ]; then
		rm -rf target
	fi
		
	echo "done"
}

# Copy all of the wars to a target directory
copyWars()
{
	echo
	echo "Copying WARs to target..."
	mkdir target
	cp -r */*/target/*.war target/
	echo "done"
}

clear
echo "Building Blueground WARs..."
echo

# Run the builds
clean
echo
install libraries ${1} "clean install -Dmaven.test.skip=true" ${3}
install common ${1} "clean install -Dmaven.test.skip=true" ${3}
install managers ${1} "clean install -Dmaven.test.skip=true" ${3}
install webservices ${1} "clean install -Dmaven.test.skip=true" ${3}
install webapps ${1} "-Pasm clean install -Dmaven.test.skip=true" ${3}
copyWars

echo
echo "Build complete"