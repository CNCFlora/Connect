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
        microAjax(api+"/api/user",function(r) {
            var u = JSON.parse(r);
            if(u.status == "approved") {
                opts.onlogin(u);
            } else {
                opts.onlogout(u);
            }
        });
    };

    window.addEventListener("message",function(msg){
        microAjax(api+"/api/auth",function(r) {
            var u = JSON.parse(r);
            if(u.status == "approved") {
                opts.onlogin(u);
                win.close();
            } else {
                win.postMessage('bad',api);
            }
        },msg.data);
    },false);
    Connect.login = function() {
        if(win != null) win.close();
        win = window.open(api+'/connect?'+location.origin,'connect','width=350,height=220,location=0,menubar=0,toolbar=0',true);
    };

    Connect.logout = function() {
        microAjax(api+"/api/logout",function(r,b){
            opts.onlogout(JSON.parse(r));
        },'foo=bar');
    };

    return Connect;

})();


/*
Copyright (c) 2008 Stefan Lange-Hegermann

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in
all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
THE SOFTWARE.
*/

function microAjax(url, callbackFunction)
{
	this.bindFunction = function (caller, object) {
		return function() {
			return caller.apply(object, [object]);
		};
	};

	this.stateChange = function (object) {
		if (this.request.readyState==4)
			this.callbackFunction(this.request.responseText);
	};

	this.getRequest = function() {
		if (window.ActiveXObject)
			return new ActiveXObject('Microsoft.XMLHTTP');
		else if (window.XMLHttpRequest)
			return new XMLHttpRequest();
		return false;
	};

	this.postBody = (arguments[2] || "");

	this.callbackFunction=callbackFunction;
	this.url=url;
	this.request = this.getRequest();
	
	if(this.request) {
		var req = this.request;
		req.onreadystatechange = this.bindFunction(this.stateChange, this);

		if (this.postBody!=="") {
			req.open("POST", url, true);
			req.setRequestHeader('X-Requested-With', 'XMLHttpRequest');
			req.setRequestHeader('Content-type', 'application/x-www-form-urlencoded');
			req.setRequestHeader('Connection', 'close');
		} else {
			req.open("GET", url, true);
		}

		req.send(this.postBody);
	}
}
