# OMAR Documentation

This repository will checkout and build documentation from all other repositories.

## Usage
Use `python3 createFiles.py` to checkout all documentation and build the necessary `mkdocs.yml` and `index.md` files.

## Adding App to Docs
To add an application to the omar-docs app simply add the name of the application under the `apps` list in `docVars` 
(please keep in alphabetical order!!!). Also note that the app name expected is the name of the app's repository in 
GitHub, not the name used on OpenShift or the Image Name.

## Using Custom Paths for Doc Files
By default, omar-docs will look in the following locations for documentation files in each application repository:
- application.yml - Will search the whole repository and inject the first one found
- Dockerfile - Will look under both `docker/Dockerfile` and `Dockerfile` and inject the file if one is found
- deploymentConfig - By default this file will NOT be included in an apps documentation unless it is specficied with 
custom variables seen below.
- install-guide - Will look under `docs/install-guide/{app}.md`. If this file does not exist, no other files will be 
injected into omar-docs
- user-guide - Will look under `docs/user-guide/{app}.md`. This file is not required for all apps,

If one would like omar-docs to look at other path names within the app's repository to find the above files, one may put
 a `docsConfig.yml` file in the root directory of their app's repository. The following variables are supported:
- applicationFile: `{custom/path/to/application.yml}`
- dockerFile: `{custom/path/to/Dockerfile}`
- displayDeployConf: `True` if you want the app's deployConfig template to be public, `False` or leave blank otherwise
- install-guide: `{custom/path/to/install-guide}`
- user-guide: `{custom/path/to/user-guide}`