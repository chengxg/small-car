export default(function() {
	let _BARCODE = "bluetooth";
	let activity = null;
	let bridge = null;
	let BluetoothAdapter,
		mAdapter,
		btManager;

	function init() {
		try {
			activity = plus.android.runtimeMainActivity();
			bridge = plus.bridge;
			BluetoothAdapter = plus.android.importClass("android.bluetooth.BluetoothAdapter");
			mAdapter = BluetoothAdapter.getDefaultAdapter();
			btManager = plus.android.invoke("site.chengxg.h5plugin.BluetoothManager", "getInstance");
			//初始化activity
			plus.android.setAttribute(btManager, "activity", activity);
		} catch(e) {
			alert("初始化蓝牙失败!");
		}
	}

	/**
	 * @callback errorCallback 错误回调
	 * @param {String} msg 错误信息
	 */
	/**
	 * 调用插件方法
	 * @param {String} methodName 调用方法名
	 * @param {Function} successCallback 成功回调
	 * @param {Function} errorCallback 失败回调
	 * @param {Object} params 传入的参数
	 */
	function asyncExecuteFunc(methodName, successCallback, errorCallback, params) {
		let success = typeof successCallback !== "function" ? null : function(data) {
			successCallback(data);
		};
		let fail = typeof errorCallback !== "function" ? null : function(msg) {
			errorCallback(msg);
		};
		let callbackID = bridge.callbackId(success, fail);
		return bridge.exec(_BARCODE, methodName, [callbackID, params]);
	}

	/**
	 * 获取蓝牙的状态
	 * @return {boolean} 是否已开启
	 */
	function getBluetoothStatus() {
		if(mAdapter != null) {
			return mAdapter.isEnabled();
		}
		return false;
	}

	/**
	 * 打开蓝牙 成功回调
	 * @callback successCallback
	 * @param {String} msg
	 */
	/**
	 * 打开蓝牙
	 * @param {successCallback}
	 * @param {errorCallback}
	 */
	function turnOnBluetooth(successCallback, errorCallback) {
		return asyncExecuteFunc("turnOnBluetooth", successCallback, errorCallback);
	}

	/**
	 * 关闭蓝牙 成功回调
	 * @callback successCallback
	 * @param {String} msg
	 */
	/**
	 * 关闭蓝牙
	 * @param {successCallback}
	 * @param {errorCallback}
	 */
	function turnOffBluetooth(successCallback, errorCallback) {
		return asyncExecuteFunc("turnOffBluetooth", successCallback, errorCallback);
	}

	/**
	 * 成功回调
	 * @callback successCallback
	 * @param {String} state STATE_TURNING_ON,STATE_ON,STATE_TURNING_OFF,STATE_OFF
	 */
	/**
	 * 监听蓝牙状态
	 * @param {successCallback}
	 * @param {errorCallback}
	 */
	function listenBluetoothStatus(successCallback, errorCallback) {
		return asyncExecuteFunc("listenBluetoothStatus", successCallback, errorCallback);
	}

	/**
	 * 得到已经配对的设备 回调
	 * @callback successCallback
	 * @param {Object[]} devices
	 * @param {String} devices[].name
	 * @param {String} devices[].address
	 */
	/**
	 * 得到已经配对的设备
	 * @param {successCallback}
	 * @param {errorCallback}
	 */
	function getPairedDevices(successCallback, errorCallback) {
		return asyncExecuteFunc("getPairedDevices", successCallback, errorCallback);
	}

	/**
	 * 发现新设备回调
	 * @callback successCallback
	 * @param {Object|String} [state=ACTION_DISCOVERY_FINISHED] devices 返回对象是蓝牙设备, 返回字符串是 搜索完成
	 * @param {String} devices.name 蓝牙名称
	 * @param {String} devices.address 蓝牙地址
	 * 
	 */
	/**
	 * 发现新设备
	 * @param {successCallback}
	 * @param {errorCallback}
	 */
	function findingNewDevices(successCallback, errorCallback) {
		return asyncExecuteFunc("findingNewDevices", successCallback, errorCallback);
	}

	/**
	 * 链接到设备 回调
	 * @callback successCallback
	 * @param {Object} devices
	 * @param {String} devices.name
	 * @param {String} devices.address
	 */
	/**
	 * 链接到设备
	 * @param {successCallback}
	 * @param {errorCallback}
	 */
	function connDevice(successCallback, errorCallback, address) {
		return asyncExecuteFunc("connDevice", successCallback, errorCallback, address);
	}

	/**
	 * 断开连接 回调
	 * @callback successCallback
	 */
	/**
	 * 链接到设备
	 * @param {successCallback}
	 * @param {errorCallback}
	 */
	function disConnDevice(successCallback, errorCallback) {
		return asyncExecuteFunc("disConnDevice", successCallback, errorCallback);
	}

	/**
	 * 读取数据 回调
	 * @callback successCallback
	 * @param {String} dataStr
	 */
	/**
	 * 读取数据
	 * @param {successCallback}
	 * @param {errorCallback}
	 * @param {Number} bufferSize 缓冲区大小
	 */
	function readData(successCallback, errorCallback, bufferSize) {
		return asyncExecuteFunc("readData", successCallback, errorCallback, bufferSize);
	}

	/**
	 * 发送数据 回调
	 * @callback successCallback
	 * @param {String} msg
	 */
	/**
	 * 发送数据
	 * @param {successCallback}
	 * @param {errorCallback}
	 */
	function sendData(successCallback, errorCallback, dataStr) {
		return asyncExecuteFunc("sendData", successCallback, errorCallback, dataStr);
	}

	return {
		init: init,
		getBluetoothStatus: getBluetoothStatus,
		turnOnBluetooth: turnOnBluetooth,
		turnOffBluetooth: turnOffBluetooth,
		listenBluetoothStatus: listenBluetoothStatus,
		getPairedDevices: getPairedDevices,
		findingNewDevices: findingNewDevices,
		connDevice: connDevice,
		disConnDevice: disConnDevice,
		readData: readData,
		sendData: sendData
	};
})();