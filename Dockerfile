FROM dockerfile/java

RUN apt-get install supervisor ruby -y
RUN gem sources -r http://rubygems.org/ && gem sources -a https://rubygems.org/ && gem install small-ops
RUN mkdir /var/log/supervisord 

RUN wget http://repo1.maven.org/maven2/org/eclipse/jetty/jetty-runner/9.2.0.M0/jetty-runner-9.2.0.M0.jar -O /root/jetty.jar
RUN mkdir /var/lib/floraconnect
ADD start.sh /root/start.sh
RUN chmod +x /root/start.sh

ADD target/flora-connect-0.2.5-standalone.war /root/connect.war

ADD supervisord.conf /etc/supervisor/conf.d/proxy.conf

EXPOSE 8080

VOLUME ["/var/floraconnect"]
CMD ["supervisord"]

