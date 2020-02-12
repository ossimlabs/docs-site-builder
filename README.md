# Generic Mkdocs Site Builder

This tool uses one project_vars.yml file to specify all the notable modules in a multi-repository project to aggregate 
doc pages into one docsite. This site, once deployed in your cluster, can support users and developers of the project.

## Project Vars



The hierarchy of documentation, some number of repos each containing some number of modules. A module is just a named 
path, it can be any valid path inside the repository. Repos takes a list of dictionaries. Each repo dictionary needs 
git_url, branch, and modules. The modules key holds a list of module dictionaries. Each module dictionary holds a name, 
path, and links (the links key is optional). Links takes a dictionary of link text to link url.