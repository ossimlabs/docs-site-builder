FROM python
RUN git clone https://github.com/ossimlabs/docs-site-builder.git &&\
    pip3 install -r /docs-site-builder/requirements.txt &&\
    mkdir /out/ /mkdocs/
CMD cd /docs-site-builder &&\
    python3 tasks/clone_repos.py &&\
    python3 tasks/generate.py &&\
    mv site /out/site
