# Flora Connect

This project is an attempt to provide a simple, unified, single sign on, authorization and authentification application for the CNCFlora network of systems.

It's based on the ideas of Mozilla Persona.

## Deploy

### Docker

Docker uses the uberwar and jetty:

    docker run -d -v /var/floraconnect:/var/floraconnect:rw -p 8080:8080 -t cncflora/connect 

If running behind a proxy:

    docker run -d -v /var/floraconnect:/var/floraconnect:rw -e PROXY=/connect -p 8080:8080 -t cncflora/connect 

### Manual

Use the (uber)war, create a folder "/var/floraconnect" on your deployment server, give it permissons to the container used user (like tomcat7 or tomcat6) and them deploy the war normally (copying to /var/lib/tomcat7/webapps or using the web manager).

Access the app and register to create the first admin.

## Usage

To authenticate other systems with Connect, here is an example:

    <script src="http://code.jquery.com/jquery-2.0.2.min.js"></script>
    <script src="http://connect-domain.com/js/connect.js"></script>
    <script>
        Connect({
            context: 'my-app', // optional, if omitted return all contexts
            onlogin: function(user) {
                // your after login code here
                // the user will have a token that you can send to the server
                // to verify, but can be used in js
                console.log(user);
                $.getJSON("http://connect-domain.com/api/token?callback=?&token="+user.token,function(verified_user){
                    console.log(verified_user);
                });
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

To run the server (need permissions on /var/floraconnect):

    lein ring server-headless

To Generate deploy artifacts:

    lein ring uberwar # war to deploy on jetty or tomcat
    lein uberjar # standalone jar

## License

Distribuited under the Apache License 2.0.

