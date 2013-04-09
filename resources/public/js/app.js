var app = function($) {
    $("input[rel=complete]").each(function(i,e){
        var input = $(e);
        input.autocomplete({
            source:"/search/"+input.attr("complete")
        });
    });

    //$(document).pjax('a','.container');

    $(".container").delegate('form#login','submit',function(){
        navigator.id.request();
        return false;
    });

    $(".container").delegate('form#logout','submit',function(){
        navigator.id.logout();
        return false;
    });

    $.get("/api/user",function(u,b){
        var audience = location.protocol+"//"+location.host;
        var mail = null;
        if(typeof u == 'object') mail = u.email;
        navigator.id.watch({
            loggedInUser: mail,
            onlogin: function(assertion) {
                $.post("/api/auth",{assertion: assertion, audience: audience },function(r){
                    var u= JSON.parse(r);
                    console.log(u);
                    if(u.status == 'approved') {
                        $("form#login .alert").hide();
                        if(location.pathname == "/login") location.href="/dashboard";
                    } else {
                        $("form#login .alert").show();
                    }
                });
            },
            onlogout: function(a){
                $.post("/api/logout",function(){
                   location.href="/";
                });
            }
        });
    });
};
