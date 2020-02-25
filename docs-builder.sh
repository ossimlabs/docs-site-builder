#!/usr/bin/env bash

check_image() {
  docker images | grep site-builder >> /dev/null
  echo $?
}

if [ "$(check_image)" -ne 0 ]
then
  echo "Image not found, building..."
  docker build docker/site-builder -t site-builder
fi

mkdir out
docker run -it --rm -v "$PWD"/../ktis-docs/ktis_vars.yml:/mkdocs-site/project_vars.yml -v "$PWD"/out/:/out -v ~/.ssh:/root/.ssh/ site-builder "$@"

mv out/site/ docker/docs-service/site/
docker build docker/docs-service -t docs-service

docker run -d -p 8080:80 docs-service