FROM python

# These steps require that the repo 'docs-site builder' is volumned into the working directory

RUN mkdir -p /docs-site-builder/src/
RUN mkdir /docs-site-builder/docker/

COPY requirements.txt /docs-site-builder/
COPY src/ /docs-site-builder/src/

RUN cd /docs-site-builder/ &&\
    pip3 install -r requirements.txt &&\
    mkdir /out/
