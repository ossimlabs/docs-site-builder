FROM python

# These steps require that the repo 'docs-site builder' is volumned into the working directory

RUN pip3 install -r docs-site-builder/requirements.txt &&\
    mkdir /out/
CMD cd docs-site-builder &&\
    python3 src/tasks/clone_repos.py &&\
    python3 src/tasks/generate.py &&\
    mv site /out/site
