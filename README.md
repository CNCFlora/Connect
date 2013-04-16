# CNCFlora-Connect

This project is an attempt to provide a unified single sign on, authorization and authentification for the CNCFlora net of systems.

It's built upon Mozilla Persona.

## First run

Deploy the war to tomcat, and run:

    curl http://domain.com/\_ca -d 'email=you@exemple.com&name=Name'

To create the first admin. Them proceed to the user interface.

## Usage

To auth other systems with Connect, here is an example:

    <script src="https://login.persona.org/include.js"></script>
    <script src="http://domain.com/js/connect.js"></script>
    <script>
        Connect.watch({
            onlogin: function(user) {
                // your after login code here
            },
            onlogout: function(){
                // your after logout code here
            }
        });
    </script>

## License

Distributed under the Eclipse Public License.

