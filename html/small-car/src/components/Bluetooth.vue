<template>
	<f7-page>
		<f7-navbar title="蓝牙连接">
			<f7-nav-right>
				<f7-link popup-close>x</f7-link>
			</f7-nav-right>
		</f7-navbar>

		<f7-block>
			<f7-list form>
				<f7-list-item title="打开蓝牙">
					<f7-toggle :checked="enableBluetooth" @change="turnOnOrOffBluetooth" slot="after"></f7-toggle>
				</f7-list-item>
			</f7-list>
			<div class="block-title">已配对的设备</div>
			<div class="list">
				<ul>
					<li v-for="(device,index) in pairedDevices" :key="index">
						<a href="#" class="item-link item-content" @click="connDevice(device)">
							<div class="item-media">
								<i v-if="connectingDevice === device" class="fa fa-spinner fa-spin fa-2x text-color-blue" aria-hidden="true"></i>
								<i v-else class="fa fa-bluetooth fa-2x" :class="{'text-color-blue':connectedDevice && connectedDevice.address === device.address}" aria-hidden="true"></i>
							</div>
							<div class="item-inner">
								<div class="item-title">{{device.name}}</div>
							</div>
						</a>
					</li>
					<!--<li>
						<a href="#" class="item-link item-content">
							<div class="item-media">
								<i class="fa fa-bluetooth fa-2x text-color-blue" aria-hidden="true"></i>
							</div>
							<div class="item-inner">
								<div class="item-title">HC-05</div>
							</div>
						</a>
					</li>-->
					<li v-if="pairedDevices.length === 0">
						<div class="item-content">
							<span>未获取到已配对的设备</span>
						</div>
					</li>
				</ul>
			</div>

			<div class="block-title">新的设备</div>
			<div class="list">
				<ul>
					<li v-for="(device,index) in newDevices" :key="index">
						<a href="#" class="item-link item-content" @click="connDevice(device)">
							<div class="item-media">
								<i v-if="connectingDevice === device" class="fa fa-spinner fa-spin fa-2x text-color-blue" aria-hidden="true"></i>
								<i v-else class="fa fa-bluetooth fa-2x" :class="{'text-color-blue':connectedDevice && connectedDevice.address === device.address}" aria-hidden="true"></i>
							</div>
							<div class="item-inner">
								<div class="item-title">{{device.name}}</div>
							</div>
						</a>
					</li>
					<li v-if="newDevices.length === 0">
						<div class="item-content">
							<span>未发现新的设备</span>
						</div>
					</li>
				</ul>
			</div>
			<button type="button" class="button button-fill button-raised button-round" v-if="!isFindding" @click="findDevices">
				<span>搜索蓝牙设备</span>
			</button>
			<button type="button" class="button button-fill button-raised button-round" v-if="isFindding">
				<i class="fa fa-spinner fa-spin" aria-hidden="true"></i>&nbsp;
				<span>正在搜索...</span>
			</button>

		</f7-block>
	</f7-page>

</template>

<script>
	import { bluetoothStore } from '@/store';

	export default {
		name: 'BluetoothManage',
		data: function() {
			return bluetoothStore
		},
		created: function() {

		},
		mounted: function() {
			let that = this;
			setTimeout(function() {
				that.init();
			}, 1000);
		},
		computed: {

		},
		props: {
			show: {
				type: Boolean,
				default: true
			},
			beforeClose: Function
		},
		watch: {
			"successMsg": function() {
				this.$f7.toast.create({
					text: this.successMsg,
					position: 'center',
					closeTimeout: 2000,
				}).open();
			},
			"errMsg": function() {
				this.$f7.toast.create({
					text: this.errMsg,
					position: 'center',
					closeTimeout: 3000,
				}).open();
			}
		},
		methods: {
			init() {
				if(typeof cxg === 'undefined' || typeof cxg.bluetoothSerial === 'undefined') {
					this.errMsg = "蓝牙插件未发现, 请在app中打开";
					return;
				}
				this.getBluetoothStatus();
				this.getPairedDevices();
				this.listenBluetoothStatus();
			},
			getBluetoothStatus() {
				let that = this;
				cxg.bluetoothSerial.getBluetoothStatus(function(state) {
					if(state == "true") {
						that.enableBluetooth = true;
					} else {
						that.enableBluetooth = false;
					}
				}, function(msg) {
					that.errMsg = msg;
				});
			},
			listenBluetoothStatus: function() {
				let that = this;
				cxg.bluetoothSerial.listenBluetoothStatus(function(state) {
					switch(state) {
						case "STATE_ON":
							that.enableBluetooth = true;
							that.getPairedDevices();
							break;
						case "STATE_OFF":
							that.enableBluetooth = false;
							break;
					}
				}, function(msg) {
					that.errMsg = msg;
				});
			},
			turnOnOrOffBluetooth: function(e, e2) {
				//this.enableBluetooth = false;
				let checked = e.target.checked;
				let that = this;
				if(this.enableBluetooth === checked) {
					return;
				}
				this.enableBluetooth = checked;
				if(checked) {
					this.turnOnBluetooth()
				} else {
					this.turnOffBluetooth();
				}
			},
			turnOnBluetooth: function() {
				let that = this;
				cxg.bluetoothSerial.turnOnBluetooth(function(msg) {
					that.successMsg = msg;
				}, function(msg) {
					that.errMsg = msg;
					setTimeout(function() {
						that.enableBluetooth = false;
					}, 100);
				});
			},
			turnOffBluetooth: function() {
				let that = this;
				cxg.bluetoothSerial.turnOffBluetooth(function(msg) {
					that.successMsg = msg;
					that.isFindding = false;
				}, function(msg) {
					that.errMsg = msg;
					setTimeout(function() {
						that.enableBluetooth = true;
					}, 100);
				});
			},
			getPairedDevices: function() {
				let that = this;
				cxg.bluetoothSerial.getPairedDevices(function(devices) {
					devices.forEach(function(item) {
						//item.connectingDevice = null;
					});
					that.pairedDevices = devices;
				}, function(msg) {
					that.errMsg = msg;
				});
			},
			findDevices: function() {
				let that = this;
				if(!this.enableBluetooth) {
					return;
				}
				if(this.isFindding) {
					return;
				}
				this.isFindding = true;
				that.newDevices = [];
				cxg.bluetoothSerial.findingNewDevices(function(data) {
					if(typeof data === 'string') {
						that.isFindding = false;
					}
					if(typeof data === 'object') {
						that.newDevices.push(data);
					}
				}, function(msg) {
					that.errMsg = msg;
					that.isFindding = false;
				});
			},
			connDevice: function(device) {
				let that = this;
				if(!this.enableBluetooth) {
					return;
				}
				if(!device) {
					that.errMsg = "请输入蓝牙设备地址!";
					return;
				}
				if(this.connectedDevice === device) {
					that.disConnDevice();
					return;
				}
				that.connectingDevice = device;
				cxg.bluetoothSerial.connDevice(function(msg) {
					that.connectingDevice = null;
					that.successMsg = msg;
					that.readData();
					that.connectedDevice = device;
				}, function(msg) {
					that.errMsg = msg;
					that.connectingDevice = null;
				}, device.address);
			},
			disConnDevice: function() {
				if(this.connectedDevice) {
					let that = this;
					that.connectingDevice = that.connectedDevice;
					cxg.bluetoothSerial.disConnDevice(function(msg) {
						that.connectedDevice = null;
						that.connectingDevice = null;
					}, function(msg) {
						that.errMsg = msg;
						that.connectingDevice = null;
					});
				}
			},
			readData: function() {
				if(!this.enableBluetooth) {
					return;
				}

				let that = this;
				cxg.bluetoothSerial.readData(function(dataStr) {
					bluetoothStore.receiveDataCallback && bluetoothStore.receiveDataCallback(dataStr);
					bluetoothStore.receiveDataStr = dataStr;
				}, function(msg) {
					that.connectedDevice = null;
					that.errMsg = msg;
				}, this.bufferSize);
			}
		},
		components: {

		}
	}
</script>

<style scoped>

</style>