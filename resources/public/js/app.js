var app = function($) {
    $("input[rel=complete]").each(function(i,e){
        var input = $(e);
        input.autocomplete({
            source:"/search/"+input.attr("complete")
        });
    });

    //$(document).pjax('a','.container');

    $(".container").delegate('form#login','submit',function(){
        Connect.login();
        return false;
    });

    $(".container").delegate('form#logout','submit',function(){
        Connect.logout();
        setTimeout(function(){
            location.href='/';
        },2000);
        return false;
    });

    if(typeof noconnect == 'undefined') {
        Connect({
            onlogin: function(u) {
                if(location.pathname == "/login") location.href="/dashboard";
            },
            onlogout: function() { }
        });
    }
};
