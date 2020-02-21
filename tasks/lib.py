import argparse
from os.path import exists
import sys


def parse_args():
    parser = argparse.ArgumentParser(description='Building doc sites for any project(s).', prog="Docs-Builder")

    parser.add_argument('--config', '-c', metavar='filename', type=str, nargs='?',
                        default='project_vars.yml',
                        help='A config file containing site_root and repos.')

    args = parser.parse_args()

    if not exists(args.config):
        raise Exception(f"The file \'{args.config}\' does not exist.", 1)

    return args
