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
		cxg.bluetoothSerial.sendData(function(msg) {

		}, function(msg) {
			that.errMsg = msg;
		}, dataStr);
	}
}

export let wifiStore = {
	ipAddress:"192.168.0.102",
	port:"8080"
}
