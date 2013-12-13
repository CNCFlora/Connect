# Flora Connect SSO App

FROM nickstenning/java7 
MAINTAINER Diogo "kid" <diogo@diogok.net>

ENV APP_USER cncflora 
ENV APP_PASS cncflora

RUN useradd -g users -s /bin/bash -m $APP_USER
RUN echo $APP_USER:$APP_PASS | chpasswd

RUN cp /etc/apt/sources.list /etc/apt/sources.list.bkp && sed -e 's/http/ftp/g' /etc/apt/sources.list.bkp > /etc/apt/sources.list
RUN apt-get update -y
RUN apt-get install curl git vim openssh-server tmux -y

RUN mkdir /var/run/sshd 

EXPOSE 22
EXPOSE 8080

ADD flora-connect.jar /root/connect.jar
RUN cp /root/connect.jar /home/$APP_USER/ && chown $APP_USER /home/$APP_USER/* && rm /root/connect.jar
RUN mkdir /var/lib/floraconnect && chown $APP_USER /var/lib/floraconnect
ADD start.sh /root/start.sh
RUN chmod +x /root/start.sh

CMD ["/root/start.sh"]

