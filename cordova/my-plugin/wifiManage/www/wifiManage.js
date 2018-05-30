cordova.define("cxg-plugin-wifiManage.wifiManage", function(require, exports, module) {
	var exec = require('cordova/exec');

	exports.coolMethod = function(arg0, success, error) {
		exec(success, error, 'WifiManage', 'coolMethod', [arg0]);
	};
	exports.connectToTcpServer = function(success, error,addres,port) {
		exec(success, error, 'WifiManage', 'connectToTcpServer', [addres,port]);
	};
	exports.disconnectTcpServer = function(success, error) {
		exec(success, error, 'WifiManage', 'disconnectTcpServer');
	};
	exports.sendData = function(success, error,dataStr) {
		exec(success, error, 'WifiManage', 'sendData',[dataStr]);
	};
	exports.readData = function(success, error, bufferSize) {
		exec(success, error, 'WifiManage', 'readData',[bufferSize]);
	};
});