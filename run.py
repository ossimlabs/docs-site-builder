#!/usr/bin/python3

from os.path import exists
import argparse
import yaml
import subprocess
from pathlib import Path

try:
    from src.tasks import clone_repos, generate, lib
except:
    from tasks import clone_repos, generate, lib


def parse_args():
    parser = argparse.ArgumentParser(description='Building doc sites for any project(s).', prog="docs-site-builder")

    parser.add_argument('tasks', metavar='task', type=str, nargs='+',
                        help='tasks to perform (clone, generate, serve)')

    parser.add_argument('--config', '-c', metavar='filename', type=str, nargs='?',
                        default='project_vars.yml',
                        help='A config file containing site_root and repos.')

    args = parser.parse_args()

    if not exists(args.config):
        raise Exception(f"The file \'{args.config}\' does not exist.", 1)
    else:
        args.config = Path(args.config).resolve()

    return args


class BuilderCLI:
    def __init__(self, args):
        self.args = args
        config_file = open(args.config, 'r')
        doc_vars = yaml.load(config_file, Loader=yaml.FullLoader)
        config_file.close()
        self.doc_vars = doc_vars

    def clone(self):
        clone_repos.main(self.args.config)

    def generate(self):
        wd = self.doc_vars["working_directory"]
        if not exists(wd):
            self.clone()
        generate.main(generate.load_vars(self.args))

    def serve(self):
        if not exists("site"):
            self.generate()
        try:
            subprocess.call("python3 -m http.server -d site/".split())
        except KeyboardInterrupt:
            print("Stopping web server....")

    def clean(self):
        print("Cleaning up....")
        subprocess.call(["rm", "-rf", self.doc_vars['working_directory'], "site", "mkdocs.yml"])


def main():
    args = parse_args()

    cli = BuilderCLI(args)
    for task in args.tasks:
        try:
            if task == "clone":
                cli.clone()
            if task == "generate":
                cli.generate()
            if task == "serve":
                cli.serve()
        finally:
            if task == "clean":
                cli.clean()


if __name__ == "__main__":
    main()
