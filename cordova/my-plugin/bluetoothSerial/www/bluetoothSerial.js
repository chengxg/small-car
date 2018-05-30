cordova.define("cxg-plugin-bluetoothSerial.bluetoothSerial", function(require, exports, module) {
	var exec = require('cordova/exec');

	exports.coolMethod = function(arg0, success, error) {
		exec(success, error, 'BluetoothSerial', 'coolMethod', [arg0]);
	};

	exports.getBluetoothStatus = function(success, error) {
		exec(success, error, 'BluetoothSerial', 'getBluetoothStatus');
	};
	exports.turnOnBluetooth = function(success, error) {
		exec(success, error, 'BluetoothSerial', 'turnOnBluetooth');
	};
	exports.turnOffBluetooth = function(success, error) {
		exec(success, error, 'BluetoothSerial', 'turnOffBluetooth');
	};
	exports.listenBluetoothStatus = function(success, error) {
		exec(success, error, 'BluetoothSerial', 'listenBluetoothStatus');
	};
	exports.getPairedDevices = function(success, error) {
		exec(success, error, 'BluetoothSerial', 'getPairedDevices');
	};
	exports.findingNewDevices = function(success, error) {
		exec(success, error, 'BluetoothSerial', 'findingNewDevices');
	};
	exports.connDevice = function(success, error, address) {
		exec(success, error, 'BluetoothSerial', 'connDevice', [address]);
	};
	exports.disConnDevice = function(success, error) {
		exec(success, error, 'BluetoothSerial', 'disConnDevice');
	};
	exports.sendData = function(success, error,dataStr) {
		exec(success, error, 'BluetoothSerial', 'sendData', [dataStr]);
	};

	exports.readData = function(success, error,bufferSize) {
		exec(success, error, 'BluetoothSerial', 'readData',[bufferSize]);
	};

});