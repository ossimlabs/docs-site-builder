import yaml
import sys
from os.path import exists
try:
    from .lib import *
except ImportError:
    from lib import *


def main(doc_vars_file):
    print('\n')
    doc_vars = yaml.load(open(doc_vars_file, 'r'), Loader=yaml.FullLoader)

    check_project_vars(doc_vars, doc_vars_file)

    all_git_urls = [(x["git_url"], x["branch"]) for x in doc_vars["repos"]]

    clone_location = doc_vars["working_directory"]

    if not (exists(clone_location)):
        os.mkdir(clone_location)
    elif len(os.listdir(clone_location)) > 0:
        print("This folder is not empty! The git clone command may complain.")

    lastdir = os.getcwd()
    os.chdir(clone_location)
    print(f"Attempting to clone repositories into {os.getcwd()}...\n\n")

    for url, branch in all_git_urls:
        print("Git subprocess: ", end='')
        sys.stdout.flush()
        os.system(f"git clone {url} --branch {branch}")
        sys.stdout.flush()
        sys.stderr.flush()

    os.chdir(lastdir)


if __name__ == "__main__":
    main(parse_args().config)
