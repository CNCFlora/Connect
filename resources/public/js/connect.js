/*
 * CNCFlora Connect Library
 * http://cncflora.jbrj.gov.br
 */

if(typeof String.prototype.endsWith !== 'function') {
    String.prototype.endsWith = function(suffix) {
        return this.indexOf(suffix, this.length - suffix.length) !== -1;
    };
}

if(typeof String.prototype.startsWith !== 'function') {
    String.prototype.startsWith = function(prefix) {
        return this.indexOf(prefix) == 0;
    };
}

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
        $.ajax({
                url: api+'/api/user?callback=?',
                type: "GET",
                dataType: 'json',
                success: function(u) {
                    if(u.status == "approved") {
                        opts.onlogin(u);
                    } else {
                        opts.onlogout(u);
                    }
                },
                beforeSend: function(x) {
                    x.withCredentials=true;
                }
            });
    };

    window.addEventListener("message",function(msg){
        $.ajax({
                url: api+'/api/auth?callback=?',
                type: "GET",
                dataType: 'json',
                data: msg.data,
                success: function(u) {
                    if(u.status == "approved") {
                        opts.onlogin(u);
                        win.close();
                    } else {
                        win.postMessage('bad',api);
                    }
                },
                beforeSend: function(x) {
                    x.withCredentials=true;
                }
            });
    },false);

    Connect.login = function() {
        if(win != null) win.close();
        win = window.open(api+'/connect?'+location.origin,'connect','width=350,height=220,location=0,menubar=0,toolbar=0',true);
    };

    Connect.logout = function() {
        $.ajax({
                url: api+'/api/logout?callback=?',
                type: "GET",
                dataType: 'json',
                success: function(u) {
                    opts.onlogout(u);
                },
                beforeSend: function(x) {
                    x.withCredentials=true;
                }
            });
    };

    return Connect;

})();

