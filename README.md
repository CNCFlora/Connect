# Flora Connect

This project is an attempt to provide a simple, unified, single sign on, authorization and authentification application for the CNCFlora network of systems.

It's based on the ideas of Mozilla Persona.

## Deploy

### Docker

Docker uses the uberwar and jetty:

docker run -d -v /var/lib/floraconnect:/var/lib/floraconnect:rw -p 8080:8080 -p 2828:22 -t cncflora/connect 

### Manual

Use the (uber)war, Create a folder "/var/lib/floraconnect" on your deployment server, give it permissons to the container used user (like tomcat7 or tomcat6) and them deploy the war normally (copying to /var/lib/tomcat7/webapps or using the web manager).

Access the app and register to create the first admin.

## Usage

To authenticate other systems with Connect, here is an example:

    <script src="http://code.jquery.com/jquery-2.0.2.min.js"></script>
    <script src="http://connect-domain.com/js/connect.js"></script>
    <script>
        Connect({
            onlogin: function(user) {
                // your after login code here
            },
            onlogout: function(){
                // your after logout code here
            }
        });
        $("#login-bt").click(Connect.login);
        $("#logout-bt").click(Connect.logout);
    </script>

## Development

You can use Vagrant, or just Leiningen.

To run the tests:
    lein midje :autotest

To run the server (need permissions on /var/lib/floraconnect):
    lein ring server-headless

To Generate deploy artifacts:
    lein ring uberwar
    lein ring uberjar

## License

Distribuited under the Apache License 2.0.

