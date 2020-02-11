import argparse
from os.path import exists
import sys


def parse_args():
    parser = argparse.ArgumentParser(description='Process some integers.')
    parser.add_argument('--config', '-c', metavar='filename', type=str, nargs='?',
                        default='docVars.yml',
                        help='A config file containing site_root and repos.')

    args = parser.parse_args()

    if not exists(args.config):
        exit_msg(f"The file \'{args.config}\' does not exist.", 1)

    return args


def exit_msg(msg, code):
    print(msg, file=sys.stderr)
    raise Exception(msg)
