#!/usr/bin/env bash

check_docker_installed() {
  command -v docker >> /dev/null
  if [[ "$?" == "1" ]]
  then
    echo "This script requires docker."
    exit 1
  fi

}

build_image_created() {
  docker images | grep site-builder >> /dev/null
  echo $?
}

help() {
  echo
  echo "Generate and/or serve a doc site given a yaml configuration file. See the README for config options."
  echo "  Usage: ./docs-site-builder.sh [config file] [tasks...]"
  echo
  echo " Tasks"
  echo "   generate   Create the site directory"
  echo "   serve      Create the webserver"
  echo "   clean      Remove all cached resources"
  echo
  exit "$1"
}

check_args() {
  if [[ $1 == "" ]]
  then
    echo "You must specify a config file!"
    help 1
  fi

  if [[ $1 == "-h" || $1 == "--help" ]]
  then
    help 0
  fi

  if [[ ! -f "$1" ]]
  then
    echo "$1 is not a regular file."
    help 1
  fi

  CONFIG_FILE=$1

  if [[ $# == 1 ]]
  then
    echo "No tasks specified"
    help 1
  fi

  shift

  while test $# -gt 0
  do
    case "$1" in
        generate) GENERATE=1
            ;;
        serve) SERVE=1
            ;;
        clean) CLEAN=1
            ;;
        *) echo "I don't recongnize task '$1'."
           help 1
            ;;
    esac
    shift
  done
}

check_docker_installed

check_args "$@"

if [[ "$GENERATE" == 1 || "$SERVE" == 1 ]]
then
  if [[ $(build_image_created) == 1 ]]
  then
    echo "Creating site-builder image..."
    docker build docker/site-builder -t site-builder
  fi

  mkdir __out
  docker run -it --rm -v "$PWD"/"$CONFIG_FILE":/docs-site-builder/project_vars.yml -v "$PWD"/__out/:/out -v ~/.ssh:/root/.ssh/ --name site-builder site-builder

  if [[ ! -d "__out/site" ]]
  then
    echo "Generation failed."
    exit 1
  fi

  if [[ "$SERVE" != 1 ]]
  then
    cp -r __out/site/ site/
  fi
fi

if [[ "$SERVE" == 1 ]]
then
  mv __out/site/ docker/docs-service/site/
  docker build docker/docs-service -t docs-service

  docker kill docs-service &> /dev/null
  docker run -d -p 8080:80 --rm --name docs-service docs-service
  echo "try opening http://localhost:8080/ now!"
fi

if [[ "$CLEAN" ]]
then
  { rm -r __out ; } &> /dev/null
  { rm -r site ; } &> /dev/null
  { docker kill site-builder ; } &> /dev/null
  { docker image rm site-builder ; } &> /dev/null
  { docker kill docs-service ; } &> /dev/null
  { docker image rm docs-service ; } &> /dev/null
fi
