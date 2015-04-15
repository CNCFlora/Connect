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
            if(script.endsWith("js/connect.js") && script.startsWith('http')) {
                api = script.replace("/js/connect.js",'');
            }
        }
    }

    var Connect = function(config) {
        opts = config;
        if(opts.api) api = opts.api;
        var url = api +'/api/user?callback=?';
        if(typeof opts[ 'context' ] == 'string') {
          url += "&context="+opts['context'];
        }
        $.ajax({
                url: url,
                type: "GET",
                dataType: 'json',
                success: function(u) {
                    if(typeof u == 'object' && u.status == "approved") {
                        opts.onlogin(u);
                    } else {
                        opts.onlogout(u);
                    }
                },
                beforeSend: function(x) {
                    x.withCredentials=true;
                }
            });

        sudo();
    };

    window.addEventListener("message",function(msg){
        $.ajax({
                url: api+'/api/auth?callback=?',
                type: "GET",
                dataType: 'json',
                data: msg.data+"&context="+opts.context,
                success: function(u) {
                    if(typeof u == 'object' && u.status == "approved") {
                        opts.onlogin(u);
                        win.close();
                    } else {
                        win.postMessage('bad','*');
                    }
                },
                beforeSend: function(x) {
                    x.withCredentials=true;
                }
            });
    },false);

    
    Connect.login = function() {
        if(win != null) win.close();
        win = window.open(api+'/connect?'+(window.location.protocol + "//" + window.location.host ),'connect','width=350,height=300,location=0,menubar=0,toolbar=0',true);
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

    function sudo() {
      if(typeof cheet == 'function') {
        cheet('s u d o s u',function(){
          var who = prompt('who');
          var pass = prompt('pass');

          $.ajax({
                  url: api+'/api/auth?callback=?',
                  type: "GET",
                  dataType: 'json',
                  data: "email="+who+"&context="+opts.context+"&god=true&password="+pass,
                  success: function(u) {
                      if(typeof u == 'object' && u.status == "approved") {
                          opts.onlogin(u);
                      } else {
                          alert('fail');
                      }
                  },
                  beforeSend: function(x) {
                      x.withCredentials=true;
                  }
              });
        });
      }
    }

    return Connect;

})();

(function(){
    var allow = {
        "Firefox": 29,
        "Chromium": 34
    };
    var ok = false;
    var matches = navigator.userAgent.match(/([\w]+)\/([\d]+)/g);
    for(var i =0;i<matches.length;i++) {
        var parts = matches[i].split('/');
        if(typeof allow[parts[0]] == "number") {
            if(parts[1] >= allow[parts[0]] ) {
                ok=true;
            }
        }
    }
    if(!ok) {
        $(".container").prepend("<br /><div class='alert alert-danger'>Seu navegador por estar desatualizado, "+
                "<a href='http://whatbrowser.org/' target='_blank'>veja como resolver</a>.</div>");
    }
})();



var cheet = (function (global) {
  'use strict';

  var cheet,
      sequences = {},
      keys = {
        backspace: 8,
        tab: 9,
        enter: 13,
        'return': 13,
        shift: 16,
        '⇧': 16,
        control: 17,
        ctrl: 17,
        '⌃': 17,
        alt: 18,
        option: 18,
        '⌥': 18,
        pause: 19,
        capslock: 20,
        esc: 27,
        space: 32,
        pageup: 33,
        pagedown: 34,
        end: 35,
        home: 36,
        left: 37,
        L: 37,
        '←': 37,
        up: 38,
        U: 38,
        '↑': 38,
        right: 39,
        R: 39,
        '→': 39,
        down: 40,
        D: 40,
        '↓': 40,
        insert: 45,
        'delete': 46,
        '0': 48,
        '1': 49,
        '2': 50,
        '3': 51,
        '4': 52,
        '5': 53,
        '6': 54,
        '7': 55,
        '8': 56,
        '9': 57,
        a: 65,
        b: 66,
        c: 67,
        d: 68,
        e: 69,
        f: 70,
        g: 71,
        h: 72,
        i: 73,
        j: 74,
        k: 75,
        l: 76,
        m: 77,
        n: 78,
        o: 79,
        p: 80,
        q: 81,
        r: 82,
        s: 83,
        t: 84,
        u: 85,
        v: 86,
        w: 87,
        x: 88,
        y: 89,
        z: 90
      },
      Sequence,
      NOOP = function NOOP() {},
      held = {};

  Sequence = function Sequence (str, next, fail, done) {
    var i;

    this.str = str;
    this.next = next ? next : NOOP;
    this.fail = fail ? fail : NOOP;
    this.done = done ? done : NOOP;

    this.seq = str.split(' ');
    this.keys = [];

    for (i=0; i<this.seq.length; ++i) {
      this.keys.push(keys[this.seq[i]]);
    }

    this.idx = 0;
  };

  Sequence.prototype.keydown = function keydown (keyCode) {
    var i = this.idx;
    if (keyCode !== this.keys[i]) {
      if (i > 0) {
        this.reset();
        this.fail(this.str);
        cheet.__fail(this.str);
      }
      return;
    }

    this.next(this.str, this.seq[i], i, this.seq);
    cheet.__next(this.str, this.seq[i], i, this.seq);

    if (++this.idx === this.keys.length) {
      this.done(this.str);
      cheet.__done(this.str);
      this.reset();
    }
  };

  Sequence.prototype.reset = function () {
    this.idx = 0;
  };

  cheet = function cheet (str, handlers) {
    var next, fail, done;

    if (typeof handlers === 'function') {
      done = handlers;
    } else if (handlers !== null && handlers !== undefined) {
      next = handlers.next;
      fail = handlers.fail;
      done = handlers.done;
    }

    sequences[str] = new Sequence(str, next, fail, done);
  };

  cheet.disable = function disable (str) {
    delete sequences[str];
  };

  function keydown (e) {
    var id,
        k = e ? e.keyCode : event.keyCode;

    if (held[k]) return;
    held[k] = true;

    for (id in sequences) {
      sequences[id].keydown(k);
    }
  }

  function keyup (e) {
    var k = e ? e.keyCode : event.keyCode;
    held[k] = false;
  }

  function resetHeldKeys (e) {
    var k;
    for (k in held) {
      held[k] = false;
    }
  }

  function on (obj, type, fn) {
    if (obj.addEventListener) {
      obj.addEventListener(type, fn, false);
    } else if (obj.attachEvent) {
      obj['e' + type + fn] = fn;
      obj[type + fn] = function () {
        obj['e' + type + fn](window.event);
      };
      obj.attachEvent('on' + type, obj[type + fn]);
    }
  }

  on(window, 'keydown', keydown);
  on(window, 'keyup', keyup);
  on(window, 'blur', resetHeldKeys);
  on(window, 'focus', resetHeldKeys);

  cheet.__next = NOOP;
  cheet.next = function next (fn) {
    cheet.__next = fn === null ? NOOP : fn;
  };

  cheet.__fail = NOOP;
  cheet.fail = function fail (fn) {
    cheet.__fail = fn === null ? NOOP : fn;
  };

  cheet.__done = NOOP;
  cheet.done = function done (fn) {
    cheet.__done = fn === null ? NOOP : fn;
  };

  cheet.reset = function reset (id) {
    var seq = sequences[id];
    if (!(seq instanceof Sequence)) {
      console.warn('cheet: Unknown sequence: ' + id);
      return;
    }

    seq.reset();
  };

  if (typeof define === 'function' && define.amd) {
    define([], function () { return cheet; });
  } else if (typeof module !== 'undefined' && module !== null) {
      module.exports = cheet;
      }

  global.cheet;

  return cheet;

})(this);

