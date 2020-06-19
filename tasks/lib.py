import argparse
import os

def parse_args():
    parser = argparse.ArgumentParser(description='Building doc sites for any project(s).', prog="docs-site-builder")

    parser.add_argument('--config', '-c', metavar='filename', type=str, nargs='?',
                        default='project_vars.yml',
                        help='A config file containing site_root and repos.')

    args = parser.parse_args()

    if not exists(args.config):
        raise Exception(f"The file \'{args.config}\' does not exist.", 1)

    return args

def check_project_vars(doc_vars, doc_vars_file):
    if "repos" not in doc_vars or type(doc_vars["repos"]) != list:
        raise Exception(f"Couldn't find a list of repos under in {doc_vars_file}. I can't do anything.", 1)

    if not os.getcwd().endswith("docs-site-builder"):
        raise Exception("Run this file from the project root, mkdocs-site/", 1)

    clone_location = doc_vars["working_directory"]

    if clone_location in ["docker", "template_files", "tasks", ".", os.getcwd()]:
        raise Exception("Bad working_directory! That folder is already used by this project.", 1)

    if clone_location == "":
        raise Exception("The working_directory variable cannot be empty.", 1)
