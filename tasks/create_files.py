import yaml
import os
import subprocess
import json
from pathlib import Path
from lib import *


def main(doc_vars_file):
    if not os.getcwd().endswith("mkdocs-site"):
        exit_msg("Run this file from the project root, mkdocs-site/", 1)

    root = Path(os.getcwd()).absolute()

    # Load variables and clone all repos
    doc_vars = yaml.load(open(doc_vars_file, 'r'), Loader=yaml.FullLoader)
    os.chdir(doc_vars["working_directory"])

    all_modules = {m["name"]: m["path"] for repo in doc_vars["repos"]
                   for m in repo["modules"]}

    # Create any custom paths for app config files that don't follow the defaults
    custom_paths = create_custom_paths(all_modules, doc_vars)

    # Create all mkdocs documents
    build_home_page(doc_vars, custom_paths, all_modules)
    createInstallGuides(doc_vars, custom_paths, all_modules)
    # os.chdir("../..")
    os.chdir(root)
    mkdocsYML(doc_vars, custom_paths, all_modules)

    # Build the mkdocs site
    buildMkdocs()


def create_custom_paths(all_modules, doc_vars):
    custom_paths = dict()

    for name, path in all_modules.items():
        check_mkdocs_config(Path(path), name, doc_vars)
        mkdocs_config = open(Path(path, "mkdocs.yml"))
        custom_paths[name] = yaml.load(mkdocs_config, Loader=yaml.FullLoader)
        mkdocs_config.close()

    return custom_paths


def check_mkdocs_config(app_root, name, docVars):
    saved_dir = os.getcwd()
    os.chdir(Path(".").absolute() / app_root)

    if not exists("docs"):
        os.mkdir("docs")

    if not exists(Path("docs", "index.md")):
        open("index.md", "w+").close()

    if not exists("mkdocs.yml"):
        default_mkdoc_config = docVars["default_mkdoc_config"]
        default_mkdoc_config["site_name"] = name

        mkdocs_config_file = open("mkdocs.yml", "w+")
        mkdocs_config_file.write(yaml.dump(default_mkdoc_config))
        mkdocs_config_file.close()

    os.chdir(saved_dir)


def build_home_page(docVars, customPaths, allModules):
    indexFile = open("index.md", 'w')
    indexFile.write("| | | | | |\n|-|-|-|-|-|\n")

    for name, _ in allModules.items():

        indexFile.write(f"| **{name}** | ")

        for guideName in docVars["linked_guides"]:
            indexFile.write("| ")

            guidePath = getGuidePath(name, customPaths, guideName, allModules)

            if exists(guidePath):
                LINK = guidePath.split(".")[0]
                print(f"Found {guidePath}...")
                indexFile.write(f"[{guideName}]({LINK}/)")

        indexFile.write("|  |\n")
        readmePath = Path(name, "README.md")

        if exists(readmePath):
            appDescription = findDescriptionLine(readmePath)
            if appDescription:
                indexFile.write("| " + appDescription + " |\n")
            else:
                print("Description not found in README.")
                indexFile.write("| Description not available. |\n")
        else:
            indexFile.write("| Description not available. |\n")

    indexFile.close()


def createInstallGuides(docVars, customPaths, allModules):
    for app in allModules:
        guidePath = getGuidePath(app, customPaths, "install-guide", allModules)
        injectAppFile(app, customPaths, guidePath)
        if app in customPaths and customPaths[app].get("displayDeployConf"):
            injectDeployConf(app, customPaths, guidePath)
        injectDockerFile(app, customPaths, guidePath)
        injectSourceCode(app, customPaths, guidePath)


def injectAppFile(app, customPaths, guidePath):
    if app in customPaths and "applicationFile" in customPaths[app]:
        configPath = f"{app}/{str(customPaths[app]['applicationFile'])}"
    else:
        configPath = subprocess.getoutput(f"find {app} -name 'application.yml' | head -1")

    if exists(guidePath) and exists(configPath):
        embedFileInGuide(guidePath, configPath, "\n\n## Application YML Configuration\n")


def injectDeployConf(app, customPaths, guidePath):
    all_configs = os.listdir("../deployment_configs")

    for config in all_configs:
        if app in config:
            configPath = "../deployment_configs/" + config
            if exists(guidePath) and exists(configPath):
                embedFileInGuide(guidePath, configPath, "\n## Example OpenShift Deployment Config\n")


def injectDockerFile(app, customPaths, guidePath):
    if app in customPaths and "dockerFile" in customPaths[app]:
        dockerPath = f"{app}/{str(customPaths[app]['dockerFile'])}"
    elif exists(app + "/docker/Dockerfile"):
        dockerPath = f"{app}/docker/Dockerfile"
    else:
        dockerPath = f"{app}/Dockerfile"

    if exists(guidePath) and exists(dockerPath):
        embedFileInGuide(guidePath, dockerPath, "\n## Dockerfile\n")


def injectSourceCode(app, customPaths, guidePath):
    if exists(guidePath):
        guideStream = open(guidePath, 'a')
        guideStream.write("\n## Source Code\n")
        gitUrl = f"https://github.com/ossimlabs/{app}.git"
        guideStream.write(f"[{gitUrl}]({gitUrl})\n")


def mkdocsYML(docVars, customPaths, allModules):
    mkdocsFile = open("mkdocs.yml", "w+")
    mkdocsFile.write(
        f"""site_name: {docVars['project_name']}
docs_dir: {docVars["working_directory"]}
extra_javascript:
- table.js
nav:
- Home: index.md
""")

    for guideName in docVars["linked_guides"]:
        FLAG = False

        for app in allModules:
            guidePath = getGuidePath(app, customPaths, guideName, allModules)

            if exists("docs" / guidePath):
                if not FLAG:
                    mkdocsFile.write(f"- {guideName}:\n")
                    FLAG = True
                mkdocsFile.write(f"  - {app}: {guidePath}\n")
    mkdocsFile.close()


def buildMkdocs():
    os.system("find . -name '*.css' -type f -delete")
    os.system("find . ! -name 'table.js' -name '*.js' -type f -delete")
    os.system("mkdocs build")


def findDescriptionLine(readmePath):
    readmeFile = open(readmePath, 'r')
    CHECK_NEXT = False

    for line in readmeFile:
        if not line.strip():
            continue

        if "## Description" in line:
            CHECK_NEXT = True
            continue

        if CHECK_NEXT:
            return line.strip()

    return None


def getGuidePath(appName, customPaths, guideName, allModules):
    if appName in customPaths and guideName in customPaths[appName]:
        guidePath = Path(allModules[appName], customPaths[appName][guideName])
    else:
        guidePath = Path(allModules[appName], "docs", guideName, f"{appName}.md")

    return guidePath


def embedFileInGuide(guidePath, filetoWrite, header):
    print(f"Writing {filetoWrite} to {guidePath}")

    guideFile = open(guidePath, 'a')

    guideFile.write(header + "```\n")
    guideFile.write(open(filetoWrite, 'r').read())
    guideFile.write("```\n")

    guideFile.close()


def convertJsontoYaml(jsonFile):
    jsonIn = json.load(open(jsonFile, 'r'))

    for config in jsonIn["items"]:
        newConfig = {"apiVersion": jsonIn["apiVersion"], "kind": jsonIn["kind"]}
        newConfig.update(config)

        yamlOut = yaml.dump(newConfig)

        open(f"deployment_configs/{config['metadata']['name']}.yml", "w+").write(yamlOut)


if __name__ == "__main__":
    parsed_args = parse_args()
    main(parsed_args.config)
