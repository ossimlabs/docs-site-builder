import os
import yaml
from pathlib import Path
from jinja2 import Environment, FileSystemLoader, select_autoescape
from tasks.lib import *


def main():
    check_path()
    project_vars = load_vars()
    generated_guides = make_generated_guides(project_vars)
    create_main_page(project_vars, generated_guides)
    create_mkdocs_config(project_vars, generated_guides)
    build()


def load_vars():
    parsed_args = parse_args()
    project_vars = yaml.load(open(parsed_args.config, 'r'), Loader=yaml.FullLoader)
    project_vars["mainpage_filename"] = "index.md"

    all_modules = {m["name"]: m for repo in project_vars["repos"]
                   for m in repo["modules"]}

    project_vars["all_modules"] = all_modules

    return project_vars


def check_path():
    if not os.getcwd().endswith("mkdocs-site"):
        exit_msg("Run this file from the project root, mkdocs-site/", 1)


def create_main_page(project_vars, guide_files):
    env = Environment(
        loader=FileSystemLoader(project_vars["template_files"]),
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
        guide = f"# {module_name} Docs"
        for doc_location_local in project_vars["docs_locations"]:
            doc_location = Path(project_vars["working_directory"], module_obj["path"], doc_location_local)
            if exists(doc_location):
                if os.path.isdir(doc_location):
                    contained_files = os.listdir(doc_location)
                    project_vars["docs_locations"] += filter(os.path.isfile, contained_files)
                else:
                    guide += f"## {doc_location_local}\n\n"
                    docfile = open(doc_location, "r")
                    guide += docfile.read()
                    docfile.close()

        guide_filename = f"{module_name.replace(' ', '-')}-guide.md"
        guide_files[module_name] = guide_filename
        guide_file = open(Path(project_vars["working_directory"], guide_filename), "w")
        guide_file.write(guide)
        guide_file.close()

    return guide_files


def create_mkdocs_config(project_vars, generated_guides):
    all_pages = [{"Home": project_vars["mainpage_filename"]}]
    all_pages.append({"Guides": generated_guides})

    config = {
        "site_name": project_vars["project_name"],
        "docs_dir": project_vars["working_directory"],
        "nav": all_pages
    }

    mkdocs_config = open("mkdocs.yml", "w")
    mkdocs_config.write(yaml.dump(config))
    mkdocs_config.close()


def build():
    os.system("mkdocs build")


if __name__ == "__main__":
    main()
