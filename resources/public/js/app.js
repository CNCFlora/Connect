var app = function(base,$) {
    $("input[rel=complete]").each(function(i,e){
        var input = $(e);
        input.autocomplete({
            source:base+"/search/"+input.attr("complete")
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
            location.href=base+'/';
        },2000);
        return false;
    });

    if(typeof noconnect == 'undefined') {
        Connect({
            api: base,
            onlogin: function(u) {
                if($("html").attr("id") == "index-page") {
                    location.href=base+"/dashboard";
                }
            },
            onlogout: function() { }
        });
    }
};
