export let bluetoothStore = {
	pairedDevices: [],
	newDevices: [],
	receiveDataStr: "",
	sendData: "",
	errMsg: "",
	successMsg: "",
	connectedDevice: null,
	connectingDevice: null, //正在连接到的设备
	isFindding: false,
	enableBluetooth: false,
	bufferSize: 16,
	receiveDataCallback: null,
	sendDataFunc: function(dataStr) {
		if(!this.enableBluetooth) {
			return;
		}
		if(!dataStr) {
			return;
		}
		if(!this.connectedDevice) {
			return;
		}
		let that = this;
		plus.bluetooth.sendData(function(msg) {

		}, function(msg) {
			that.errMsg = msg;
		}, dataStr);
	}
}