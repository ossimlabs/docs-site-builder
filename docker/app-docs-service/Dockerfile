FROM centos
ENV HOME=/home/httpd
USER root
RUN mkdir -p $HOME
RUN yum --disablerepo=centos* -y install hostname
RUN yum --disablerepo=centos* -y install httpd
RUN yum --disablerepo=centos* -y install gettext
RUN yum -y install nss_wrapper
RUN yum clean all

RUN setcap cap_net_bind_service+ep /usr/sbin/httpd
RUN setcap cap_net_bind_service+ep /usr/sbin/apachectl

RUN echo 'Alias "/omar-docs" "/var/www/html"' >> /etc/httpd/conf.d/docs.conf
COPY docs.tgz /var/www/html/

WORKDIR  /var/www/html
RUN tar xfz docs.tgz
RUN mv ./site/* .
RUN rm -rf site docs.tgz

ADD run-httpd.sh $HOME/run-httpd.sh
RUN chmod -v +x $HOME/run-httpd.sh
RUN chmod 770 /usr/share/httpd
RUN chmod 770 /var/run/httpd
RUN chown 1001:0 /run/httpd
RUN chown 1001:0 /var/log/httpd
RUN chmod 770 /var/log/httpd
RUN chmod g+s /var/run/httpd
RUN find /usr/share/httpd -type d -exec chmod g+xws {} \;
RUN find /usr/share/httpd -type f -exec chmod g+rw {} \;
RUN find /etc/httpd -type d -exec chmod g+rw {} \;
RUN find /etc/pki -type d -exec chmod g+xws {} \;
RUN find /etc/pki -type f -exec chmod g+rw {} \;
RUN chown -R 1001:0 /usr/share/httpd
RUN chown -R 1001:0 /etc/httpd
RUN chown -R 1001:0 /etc/pki
RUN find $HOME -type d -exec chmod g+xws {} \;
RUN find $HOME -type f -exec chmod g+rw {} \;
RUN chown -R 1001:0 $HOME
RUN chmod 770 $HOME/*.sh
RUN chown -R 1001:0 $HOME

VOLUME /dev/random
EXPOSE 80 443
WORKDIR $HOME
USER 1001
CMD /home/omar/run-httpd.sh
