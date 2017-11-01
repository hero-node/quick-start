var extend = [
	'http://sdkjs.oss-cn-shanghai.aliyuncs.com/hybrid_jssdk.js?v=9',
	'js/ke.js',
]
var extend_debug = [
	'http://sdkjs.oss-cn-shanghai.aliyuncs.com/hybrid_jssdk.js?v=9',
	'js/ke.js',
    '/socket.io/socket.io.js',
    'js/autoRefresh.js',
]
var debug = Hero.getInitData().test;
debug = debug || window.location.href.indexOf('localhost') > -1;

var jss = debug ? extend_debug : extend;
for (var i = 0; i < jss.length; i++) {
	var tmpPath = jss[i];
	Hero.loadScriptSync(tmpPath);
};
       
