var app = function($) {
    $("input[rel=complete]").each(function(i,e){
        var input = $(e);
        input.autocomplete({
            source:"/search/"+input.attr("complete")
        });
    });
};
