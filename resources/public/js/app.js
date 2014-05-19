var app = function(base,$) {
    $("input[rel=complete]").each(function(i,e){
        var input = $(e);
        input.autocomplete({
            source:base+"/search/"+input.attr("complete")
        });
    });

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
                if($("html").attr("id") == "index-page" || $("html").attr("id") == "login-test-page") {
                    location.href=base+"/dashboard";
                }
            },
            onlogout: function() { }
        });
    }

    $("#register-page form").submit(function(){
         var missing = []
         $("#register-page form input").each(function(i,e){
            var el = $(e);
            if(el.parent().text().trim() == "*") {
                if(el.val().trim().length < 1) {
                    missing.push(el.attr("placeholder"));
                }
            }
         });
         if(missing.length >= 1) {
             alert("É necessário preencher os sequintes: "+ missing.join(";"));
             return false;
         } else {
             return true;
         }
    });

};
