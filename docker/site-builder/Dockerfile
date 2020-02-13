FROM python
RUN git clone https://github.com/ossimlabs/mkdocs-site.git &&\
    pip3 install -r /mkdocs-site/requirements.txt &&\
    mkdir /out/
CMD cd /mkdocs-site &&\
    python3 tasks/clone_repos.py &&\
    python3 tasks/generate.py &&\
    mv site /out/site
