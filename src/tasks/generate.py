import subprocess
import sys
import re
from os import getcwd, listdir
from os.path import isdir, isfile, exists
import yaml
from pathlib import Path
from jinja2 import Environment, FileSystemLoader, select_autoescape
try:
    from .lib import parse_args
except ImportError:
    from lib import parse_args


def main(project_vars):
    check_environment(project_vars)
    generated_guides = make_generated_guides(project_vars)
    create_main_page(project_vars, generated_guides)
    create_mkdocs_config(project_vars, generated_guides)
    mkdocs_build()


def load_vars(parsed_args):
    config_file = open(parsed_args.config, 'r')
    project_vars = yaml.load(config_file, Loader=yaml.FullLoader)
    config_file.close()

    project_vars["mainpage_filename"] = "index.md"

    all_modules = {m["name"]: m for repo in project_vars["repos"]
                   for m in repo["modules"]}

    project_vars["all_modules"] = all_modules

    return project_vars


def check_environment(project_vars):
    if not getcwd().endswith("docs-site-builder"):
        print(f"PWD is '{getcwd()}'. Run this file from the project root, docs-site-builder/", file=sys.stderr)
        exit(1)

    if not exists(project_vars["working_directory"]):
        print("Working directory not found... Have you run clone_repos.py?", file=sys.stderr)
        exit(1)

    if len(listdir(project_vars["working_directory"])) == 0:
        print("Working directory is empty... Have you run clone_repos.py?", file=sys.stderr)
        exit(1)

    bad_syntax = False

    for repo_index, repo in enumerate(project_vars['repos']):
        for module_index, module in enumerate(repo['modules']):
            if 'links' in module:
                if type(module['links']) != dict:
                    print(f"I expected a dictionary of links, repos.{repo_index}.{module['name']}.{module_index}.links has a {type(module['links'])}.", file=sys.stderr)
                    bad_syntax = True

    if bad_syntax:
        exit(1)


def create_main_page(project_vars, guide_files):
    env = Environment(
        loader=FileSystemLoader("template_files"),
        autoescape=select_autoescape(['html', 'xml'])
    )

    template = env.get_template("index.md.jinja2")

    combined_vars = project_vars
    combined_vars.update({"all_guides": guide_files})

    rendered_page = template.render(combined_vars)
    mainpage = open(Path(project_vars["working_directory"], project_vars["mainpage_filename"]), "w")
    mainpage.write(rendered_page)
    mainpage.close()


def make_generated_guides(project_vars):
    guide_files = {}

    for module_name, module_obj in project_vars["all_modules"].items():
        guide = f"# {module_name} Docs\n"
        module_root = Path(project_vars["working_directory"], module_obj["path"])
        for sought_doc_file_or_dir in project_vars["docs_locations"]:
            if exists_case_insensitive(module_root, sought_doc_file_or_dir):
                existing_doc_file_or_dir = get_real_path(module_root, sought_doc_file_or_dir)
                if isdir(existing_doc_file_or_dir):
                    doc_folder = existing_doc_file_or_dir
                    for doc_file in filter(lambda x: isfile(Path(doc_folder, x)), listdir(doc_folder)):
                        guide += read_docfile(Path(doc_folder, doc_file), sought_doc_file_or_dir)
                else:
                    guide += read_docfile(existing_doc_file_or_dir, sought_doc_file_or_dir)

        guide_filename = f"{module_name.replace(' ', '-')}-guide.md"
        guide_files[module_name] = guide_filename
        guide_file = open(Path(project_vars["working_directory"], guide_filename), "w")
        guide_file.write(guide)
        guide_file.close()

    return guide_files


def exists_case_insensitive(parent_dir, fuzzy_name):
    if len(Path(fuzzy_name).parts) > 1:
        return exists_case_insensitive(parent_dir, Path(fuzzy_name).parent) \
               and exists_case_insensitive(Path(parent_dir, Path(fuzzy_name).parent), Path(fuzzy_name).name)

    for child_file_or_dir in listdir(parent_dir):
        if str(Path(child_file_or_dir)).lower() == str(Path(fuzzy_name)).lower():
            return True

    return False


def get_real_path(parent_dir, fuzzy_name):
    if len(Path(fuzzy_name).parts) > 1:
        direct_parent = get_real_path(parent_dir, Path(fuzzy_name).parent)
        return get_real_path(direct_parent, Path(fuzzy_name).name)

    for child_file_or_dir in listdir(parent_dir):
        if str(Path(child_file_or_dir)).lower() == str(Path(fuzzy_name)).lower():
            return Path(parent_dir, child_file_or_dir)
    print(f"get_real_path(): I couldn't find {fuzzy_name} in {parent_dir} even though I should have.", file=sys.stderr)
    exit(1)


def read_docfile(doc_file, doc_title):
    subsection = f"## {doc_title}\n\n"
    with open(doc_file, "r") as opened_doc_file:
        if str(doc_file).endswith(".md"):
            raw_markdown = opened_doc_file.read()
            fixed_backticks = re.sub('([^\n])```', r'\1\n```', raw_markdown)
            formatted_markdown = fixed_backticks + '\n\n'
            subsection += formatted_markdown
        else:
            try:
                raw_doc = opened_doc_file.read()
                stripped_backticks = raw_doc.replace('`', '')
                formatted_doc = f"```\n{stripped_backticks}\n```\n"
                subsection += formatted_doc
            except UnicodeDecodeError as e:
                print(f"Skipping unreadable file {doc_file}....")
    return subsection


def create_mkdocs_config(project_vars, generated_guides):
    all_pages = [{"Home": project_vars["mainpage_filename"]}, {"Guides": generated_guides}]

    config = {
        "site_name": project_vars["project_name"],
        "theme": "readthedocs",
        "docs_dir": project_vars["working_directory"],
        "nav": all_pages
    }

    mkdocs_config = open("mkdocs.yml", "w")
    mkdocs_config.write(yaml.dump(config))
    mkdocs_config.close()


def mkdocs_build():
    sys.argv = ["mkdocs", "build"]
    try:
        subprocess.call(["mkdocs", "build"])
    except Exception as e:
        print("--- Exception ---")
        print(e)
        print("---    End    ---")
    except:
        print("Oh well...")


if __name__ == "__main__":
    main(load_vars(parse_args()))
