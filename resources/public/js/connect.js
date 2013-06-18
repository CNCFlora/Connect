/*
 * CNCFlora Connect Library
 * http://cncflora.jbrj.gov.br
 */

var Connect = (function() {
    var scripts = document.getElementsByTagName('script');
    var audience = location.protocol+"//"+location.host;
    var opts = {};
    var win=null;
    var api = "";
    for(var s =0;s<scripts.length;s++) {
        var script = scripts[s].getAttribute("src");
        if(script != null) {
            if(script.endsWith("js/connect.js") && !script.startsWith(audience + '/') && script.startsWith('http')) {
                api = script.replace("/js/connect.js",'');
            }
        }
    }

    var Connect = function(config) {
        opts = config;
        $.getJSON(api+'/api/user?callback=?',function(u) {
            if(u.status == "approved") {
                opts.onlogin(u);
            } else {
                opts.onlogout(u);
            }
        });
    };

    window.addEventListener("message",function(msg){
        $.post(api+'/api/auth',msg.data,function(r) {
            var u = JSON.parse(r);
            if(u.status == "approved") {
                opts.onlogin(u);
                win.close();
            } else {
                win.postMessage('bad',api);
            }
        });
    },false);
    Connect.login = function() {
        if(win != null) win.close();
        win = window.open(api+'/connect?'+location.origin,'connect','width=350,height=220,location=0,menubar=0,toolbar=0',true);
    };

    Connect.logout = function() {
        $.post(api+"/api/logout",'',function(r,b){
            opts.onlogout(JSON.parse(r));
        });
    };

    return Connect;

})();

