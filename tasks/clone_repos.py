import yaml
import os
from lib import *


def main(doc_vars_file):
    print('\n')
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
    elif len(os.listdir(clone_location)) > 0:
        print("This folder is not empty! The git clone command may complain.")

    os.chdir(clone_location)
    print(f"Attempting to clone repositories into {os.getcwd()}...\n\n")

    for url, branch in all_git_urls:
        print("Git: ", end='')
        os.system(f"git clone {url} --branch {branch}")


if __name__ == "__main__":
    parsed_args = parse_args()
    main(parsed_args.config)
