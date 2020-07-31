FROM python
RUN git clone https://github.com/ossimlabs/docs-site-builder.git &&\
    pip3 install -r /docs-site-builder/requirements.txt &&\
    mkdir /out/
CMD cd /docs-site-builder &&\
    python3 src/tasks/clone_repos.py &&\
    python3 src/tasks/generate.py &&\
    mv site /out/site
