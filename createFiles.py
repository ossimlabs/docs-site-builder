import yaml
import os
import subprocess
import json
from pathlib import Path
from os.path import exists


def main():
    # Create OpenShift DeployConfig Files
    # getDeployConfigs()

    # Load variables and clone all repos
    docVars = yaml.load(open("docVars.yml", 'r'), Loader=yaml.FullLoader)
    os.chdir("docs")
    # addRepoNames(docVars)
    checkoutRepos(docVars)

    # Create any custom paths for app config files that don't follow the defaults
    customPaths = createCustomPaths(docVars)

    # Create all mkdocs documents
    homePage(docVars, customPaths)
    createInstallGuides(docVars, customPaths)
    os.chdir("..")
    mkdocsYML(docVars, customPaths)

    # Build the mkdocs site
    buildMkdocs()


# def getDeployConfigs():
#     if not exists("deployment_configs"):
#         os.mkdir("deployment_configs")
#
#     token = subprocess.getoutput("oc whoami -t")
#     getConfigsCmd = "curl -H 'Authorization: Bearer {}' -k -L -o deployment_configs/deploymentConfigs.json {}/oapi/v1/namespaces/omar-dev/deploymentconfigs".format(
#         token, os.environ["OPENSHIFT_URL"])
#     os.system(getConfigsCmd)
#
#     convertJsontoYaml("deployment_configs/deploymentConfigs.json")

#
# def addRepoNames(docVars):
#     docVars["repos"] = list()
#     GIT_PUBLIC_SERVER_URL = os.environ["GIT_PUBLIC_SERVER_URL"]
#
#     for app in docVars["apps"]:
#         docVars["repos"].append("{}/{}.git".format(GIT_PUBLIC_SERVER_URL, app))


def checkoutRepos(docVars):
    for repo, branch in docVars["repos"].items():
        os.system(f"git clone {repo} --branch {branch}")


def createCustomPaths(docVars):
    customPaths = dict()

    for app, root in docVars["apps"].items():
        check_mkdocs_config(Path(root))
        customPaths[app] = yaml.load(open(Path(root, "mkdocs.yml")), Loader=yaml.FullLoader)

        # docsConfigFile = Path(app, "docsConfig.yml")
        # if exists(docsConfigFile):
        #     customPaths[app] = yaml.load(open(docsConfigFile, 'r'), Loader=yaml.FullLoader)

    return customPaths


def check_mkdocs_config(app_root):
    saved_dir = os.getcwd()
    os.chdir(Path(".").absolute() / app_root)

    if not exists("docs"):
        os.mkdir("docs")

    if not exists(Path("docs", "index.md")):
        open("index.md", "w+").close()

    if not exists("mkdocs.yml"):
        mkdocs_config_file = open("mkdocs.yml", "w+")
        repoName = app_root.parts[-1]
        mkdocs_config_file.write(f"site_name: \"{repoName}\"\nnav:\n- Home: index.md\n")
        mkdocs_config_file.close()

    os.chdir(saved_dir)


def homePage(docVars, customPaths):
    indexFile = open("index.md", 'a')
    indexFile.write("| | | | | |\n|-|-|-|-|-|\n")

    for app, root in docVars["apps"].items():
        indexFile.write(f"| **{app}** | ")

        for guideName in docVars["guides"]:
            indexFile.write("| ")

            guidePath = getGuidePath(app, customPaths, guideName, docVars)

            if exists(guidePath):
                LINK = guidePath.split(".")[0]
                print(f"Found {guidePath}...")
                indexFile.write(f"[{guideName}]({LINK}/)")

        indexFile.write("|  |\n")
        readmePath = Path(app, "/README.md")

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


def createInstallGuides(docVars, customPaths):
    for app in docVars["apps"]:
        guidePath = getGuidePath(app, customPaths, "install-guide", docVars)
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


def mkdocsYML(docVars, customPaths):
    mkdocsFile = open("mkdocs.yml", "w+")
    mkdocsFile.write("site_name: O2 Web Services\nextra_javascript:\n- table.js\npages:\n- Home: index.md\n")

    for guideName in docVars["guides"]:
        FLAG = False

        for app in docVars["apps"]:
            guidePath = getGuidePath(app, customPaths, guideName, docVars)

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


def getGuidePath(app, customPaths, guideName, docVars):
    if app in customPaths and guideName in customPaths[app]:
        guidePath = Path(docVars["apps"][app], customPaths[app][guideName])
    else:
        guidePath = Path(docVars['apps'][app], "docs", guideName, f"{app}.md")

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
    main()
