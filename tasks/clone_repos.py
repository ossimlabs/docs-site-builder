import yaml
import os
from lib import *


def main(doc_vars_file):
    doc_vars = yaml.load(open(doc_vars_file, 'r'), Loader=yaml.FullLoader)

    all_git_urls = [(x["git_url"], x["branch"]) for x in doc_vars["repos"]]

    clone_location = doc_vars["working_directory"]

    if not os.getcwd().endswith("mkdocs-site"):
        exit_msg("Run this file from the project root, mkdocs-site/", 1)

    if clone_location in ["docker", "template_files", "tasks", ".", os.getcwd()]:
        exit_msg("Bad working_directory! That folder is already used by this project.", 1)

    if clone_location == "":
        exit_msg("The working_directory variable cannot be empty.", 1)

    if not (exists(clone_location)):
        os.mkdir(clone_location)

    os.chdir(clone_location)
    print(f"Cloning repos in directory {os.getcwd()}...")

    for url, branch in all_git_urls:
        os.system(f"git clone {url} --branch {branch}")


if __name__ == "__main__":
    parsed_args = parse_args()
    main(parsed_args.config)
