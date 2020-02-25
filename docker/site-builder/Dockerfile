FROM python
RUN git clone https://github.com/ossimlabs/docs-builder.git &&\
    pip3 install -r /docs-builder/requirements.txt &&\
    mkdir /out/
CMD cd /mkdocs-site &&\
    python3 tasks/clone_repos.py &&\
    python3 tasks/generate.py &&\
    mv site /out/site
