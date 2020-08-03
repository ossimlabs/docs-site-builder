import argparse
import os
import sys
from os.path import exists

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
        print(f"Couldn't find a list of repos under in {doc_vars_file}. I can't do anything.", file=sys.stderr)
        exit(1)

    clone_location = doc_vars["working_directory"]

    if clone_location in ["docker", "template_files", "tasks", ".", os.getcwd()]:
        print("Bad working_directory! That folder is already used by this project.", file=sys.stderr)
        exit(1)

    if clone_location == "":
        print("The working_directory variable cannot be empty.", file=sys.stderr)
        exit(1)
