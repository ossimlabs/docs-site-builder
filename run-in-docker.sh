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
  echo " Options"
  echo "  -h, --help    Display this help page"
  echo "  -c, --config  Specify the config file (Required for the tasks: generate, serve)."
  echo
  echo " Tasks (At least one task must be specified)"
  echo "   generate     Create the site directory"
  echo "   serve        Create the webserver"
  echo "   clean        Remove all cached resources"
  echo
  exit "$1"
}

check_args() {

  PARAMS=""
  while (( "$#" )); do
    case "$1" in
      -h|--help)
            help 0
            ;;
      -c|--config)
        if [ -n "$2" ] && [ "${2:0:1}" != "-" ]; then
          CONFIG_FILE=$2
          shift 2
        else
          echo "Error: Argument for $1 is missing" >&2
          exit 1
        fi
        ;;
      -*=) # unsupported flags
        echo "Error: Unsupported flag $1" >&2
        exit 1
        ;;
      *) # preserve positional arguments
        PARAMS="$PARAMS $1"
        shift
        ;;
    esac
  done

  check_positional_args $PARAMS
}

check_positional_args() {
  while test $# -gt 0
  do
    case "$1" in
        generate)
          GENERATE=1
          ;;
        serve)
          GENERATE=1
          SERVE=1
          ;;
        clean)
          CLEAN=1
          ;;
        "") ;;
        *)
          echo "I don't recongnize task '$1'." >&2
          exit 1
          ;;
    esac
    shift
  done

  if ([ -n $GENERATE ] || [ -n $SERVE ]) && [ -z CONFIG_FILE ];
  then
    echo "The tasks generate and server require a config file to be specified." >&2
  fi

  if [ -z $GENERATE ] && [ -z $SERVE ] && [ -z $CLEAN ];
  then
    echo "You must specify a task. [generate, serve, clean]" >&2
    exit 1
  fi
}

check_docker_installed

check_args "$@"

if [[ "$GENERATE" == 1 ]]
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
