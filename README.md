# CNCFlora-Connect

This project is an attempt to provide a simple, unified, single sign on, authorization and authentification for the CNCFlora net of systems.

It's based on the ideas of Mozilla Persona.

## First run

Create a folder "/var/floraconnect" on your deploy server, give it permissons to the container user and them deploy the war.

Access the app and register to create the first admin.

## Usage

To auth other systems with Connect, here is an example:

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

## License

Distributed under the Eclipse Public License.

