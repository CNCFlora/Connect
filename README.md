# CNCFlora-Connect

This project is an attempt to provide a simple, unified, single sign on, authorization and authentification for the CNCFlora net of systems.

It's built upon Mozilla Persona.

## First run

Deploy the war to tomcat, and run:

    curl http://domain.com/\_ca -d 'email=you@exemple.com&name=Name'

Or access the app and register to create the first admin.

## Usage

To auth other systems with Connect, here is an example:

    <script src="http://code.jquery.com/jquery-2.0.2.min.js"></script>
    <script src="http://domain.com/js/connect.js"></script>
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

