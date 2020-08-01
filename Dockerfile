FROM python

RUN mkdir /docs-site-builder
RUN mkdir /docs-site-builder/src
RUN mkdir /docs-site-builder/docker

COPY src/ /docs-site-builder/src/
COPY requirements.txt /docs-site-builder/
COPY docker/ /docs-site-builder/docker/

RUN pip3 install -r /docs-site-builder/requirements.txt &&\
    mkdir /out/
CMD cd /docs-site-builder &&\
    python3 src/tasks/clone_repos.py &&\
    python3 src/tasks/generate.py &&\
    mv site /out/site
