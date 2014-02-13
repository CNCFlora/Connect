# Flora Connect SSO App

FROM nickstenning/java7 
MAINTAINER Diogo "kid" <diogo@diogok.net>

ENV APP_USER cncflora 
ENV APP_PASS cncflora

RUN cp /etc/apt/sources.list /etc/apt/sources.list.bkp && sed -e 's/http/ftp/g' /etc/apt/sources.list.bkp > /etc/apt/sources.list
RUN apt-get update -y
RUN apt-get install curl git vim openssh-server tmux sudo aptitude screen wget htop default-jre-headless -y

RUN useradd -g users -G sudo -s /bin/bash -m $APP_USER
RUN echo $APP_USER:$APP_PASS | chpasswd
RUN mkdir /var/run/sshd 
RUN chmod 755 /var/run/sshd

EXPOSE 22
EXPOSE 8080

ADD target/flora-connect-0.0.1-standalone.jar /root/connect.jar
RUN cp /root/connect.jar /home/$APP_USER/ && chown $APP_USER /home/$APP_USER/* && rm /root/connect.jar
RUN mkdir /var/lib/floraconnect && chown $APP_USER /var/lib/floraconnect
ADD start.sh /root/start.sh
RUN chmod +x /root/start.sh
ADD register.sh /root/register.sh
RUN chmod +x /root/register.sh

CMD ["/root/start.sh"]

