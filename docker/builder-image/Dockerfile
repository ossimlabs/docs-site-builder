FROM python

# These steps require that the repo 'docs-site builder' is volumned into the working directory

RUN mkdir /docs-site-builder/

COPY requirements.txt /docs-site-builder/
COPY src/ /docs-site-builder/

RUN cd /docs-site-builder/ &&\
    pip3 install -r requirements.txt &&\
    mkdir /out/

CMD cd /docs-site-builder/ &&\
    python3 src/tasks/clone_repos.py &&\
    python3 src/tasks/generate.py &&\
    mv site /out/site
