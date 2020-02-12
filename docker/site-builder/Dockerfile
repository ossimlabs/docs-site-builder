ARG PROJECT_ROOT
FROM python
EXPOSE 80
VOLUME "${PROJECT_ROOT}/mkdocs-site"
RUN pip install mkdocs
#ENTRYPOINT "python3 ${PROJECT_ROOT}/mkdocs-site/createFiles.py"
