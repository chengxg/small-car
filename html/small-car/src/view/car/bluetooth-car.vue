<template>
	<f7-page>
		<f7-navbar v-if="!isShowRocker" title="蓝牙小车控制" back-link="Back"></f7-navbar>

		<div class="list">
			<ul>
				<li>
					<a href="#" class="item-link item-content popup-open" data-popup="#bluetooth">
						<div class="item-media">
							<i class="fa fa-bluetooth fa-2x" :class="{'text-color-blue':bluetoothStore.connectedDevice}" aria-hidden="true"></i>
						</div>
						<div class="item-inner">
							<div class="item-title">
								<span v-if="bluetoothStore.connectedDevice">已连接到 {{bluetoothStore.connectedDevice.name}}</span>
								<span v-else>请连接到蓝牙设备</span>
							</div>
						</div>
					</a>
				</li>
			</ul>
		</div>

		<f7-block-title>
			<div class="row" style="display: flex;align-items: center;">
				<div class="col">设置小车模式</div>
				<div class="" style="text-align: right;">
					<button class="col button button-outline button-raised" @click="readCarParamFromSinglechip" style="padding: 2px;height: 24px;line-height: 18px;width: 100px;margin: 0;">
						<i class="fa fa-refresh" aria-hidden="true"></i>
						同步参数
					</button>
				</div>
			</div>
		</f7-block-title>
		<f7-list>
			<f7-list-item radio v-for="(mode,index) in carModeOptions" :key="index" :value="mode.value" :title="mode.label" :checked="mode.value === carSetting.carMode" @change="onChageCarMode" name="carMode"></f7-list-item>
		</f7-list>

		<f7-block-title>
			自动寻迹设置
		</f7-block-title>
		<f7-list>
			<f7-list-item>
				<f7-label>寻迹最大速度: {{carSetting.atMaxSpeed}}</f7-label>

				<f7-input>
					<f7-range min="0" max="255" :value="carSetting.atMaxSpeed" step="1" :label="true" @range:change="onChangeAtMaxSpeed"></f7-range>
				</f7-input>
			</f7-list-item>
			<f7-list-item>
				<f7-label>转弯最大角度: {{carSetting.atTurnMaxDeg}}</f7-label>
				<f7-input>
					<f7-range min="0" max="60" :value="carSetting.atTurnMaxDeg" step="1" :label="true" @range:change="onChangeAtTurnMaxDeg"></f7-range>
				</f7-input>
			</f7-list-item>
		</f7-list>

		<f7-block-title>
			手动遥控
		</f7-block-title>
		<f7-block strong>
			<f7-row tag="p">
				<button type="button" class="button button-fill button-raised col" @click="openRocker">
					<i class="fa fa-car" aria-hidden="true"></i>
					遥控小车
				</button>
				<div class="col"></div>
			</f7-row>
		</f7-block>

		<!-- Popup -->
		<f7-popup id="bluetooth">
			<f7-view>
				<Bluetooth></Bluetooth>
			</f7-view>
		</f7-popup>

		<Rocker v-if="isShowRocker" :beforeClose="closeRocker"></Rocker>

	</f7-page>
</template>

<script>
	import { bluetoothStore } from '@/store';
	import Bluetooth from '@/components/Bluetooth';
	import Rocker from '@/components/Rocker';

	let setAtTurnMaxDegTimeId = 0;
	let setAtMaxSpeedTimeId = 0;

	export default {
		name: 'BluetoothCar',
		data: function() {
			return {
				bluetoothStore: bluetoothStore,
				carModeOptions: [{
						label: '未知',
						value: '0'
					}, {
						label: '手动遥控',
						value: '1'
					},
					{
						label: '自动寻迹',
						value: '2'
					},
					{
						label: '自动避障',
						value: '3'
					}
				],
				carSetting: {
					carMode: "0",
					oldAtTurnMaxDeg: 0,
					oldAtMaxSpeed: 0,
					atTurnMaxDeg: 0,
					atMaxSpeed: 0
				},
				isShowRocker: false,
				msg: "",
			}
		},
		created: function() {
			let that = this;
		},
		watch: {

		},
		methods: {
			closeRocker: function() {
				this.isShowRocker = false;
			},
			openRocker: function() {
				this.isShowRocker = true;
			},
			onChageCarMode(e) {
				let that = this;
				let mode = e.target.value;
				let oldModel = that.carSetting.carMode;
				that.carSetting.carMode = mode;

				let carModeCommand = "[cm:" + this.carSetting.carMode + "]";
				let sendPromise = this.sendDataToSinglechip(carModeCommand, true);
				sendPromise.then(function() {
					that.carSetting.carMode = mode;
				}, function(err) {
					that.$f7.toast.create({
						text: err,
						position: 'center',
						closeTimeout: 3000,
					}).open();
					setTimeout(function() {
						that.carSetting.carMode = oldModel;
					}, 300);
				})
			},
			onChangeAtMaxSpeed(val) {
				let that = this;
				that.carSetting.atMaxSpeed = val;
				if(that.carSetting.atMaxSpeed === that.carSetting.oldAtMaxSpeed) {
					return;
				}
				clearTimeout(setAtMaxSpeedTimeId);
				setAtMaxSpeedTimeId = setTimeout(function() {
					let command = "[tms:" + that.carSetting.atMaxSpeed + "]";
					let sendPromise = that.sendDataToSinglechip(command, true);
					sendPromise.then(function() {
						that.carSetting.oldAtMaxSpeed = that.carSetting.atMaxSpeed;
					}, function(err) {
						that.$f7.toast.create({
							text: err,
							position: 'center',
							closeTimeout: 2000,
						}).open();
						that.carSetting.atMaxSpeed = that.carSetting.oldAtMaxSpeed;
					})
				}, 1000);
			},
			onChangeAtTurnMaxDeg(val) {
				let that = this;
				that.carSetting.atTurnMaxDeg = val;
				if(that.carSetting.atTurnMaxDeg === that.carSetting.oldAtTurnMaxDeg) {
					return;
				}

				clearTimeout(setAtTurnMaxDegTimeId);
				setAtTurnMaxDegTimeId = setTimeout(function() {
					let command = "[tmd:" + that.carSetting.atTurnMaxDeg + "]";
					let sendPromise = that.sendDataToSinglechip(command, true);
					sendPromise.then(function() {
						that.carSetting.oldAtTurnMaxDeg = that.carSetting.atTurnMaxDeg;
					}, function(err) {
						that.$f7.toast.create({
							text: err,
							position: 'center',
							closeTimeout: 2000,
						}).open();
						that.carSetting.atTurnMaxDeg = that.carSetting.oldAtTurnMaxDeg;
					})
				}, 1000)
			},
			readCarParamFromSinglechip() {
				let that = this;

				new Promise(function(resolve, reject) {
					if(!bluetoothStore.enableBluetooth) {
						reject("未打开蓝牙");
						return;
					}
					if(!bluetoothStore.connectedDevice) {
						reject("当前未连接到蓝牙设备");
						return;
					}
					let dataStr = "[r-p:0]";

					cxg.bluetoothSerial.sendData(function() {
						let receiveDataStr = "";
						bluetoothStore.receiveDataCallback = function(data) {
							receiveDataStr += data;
							if(data.indexOf("]") > -1) {
								clearTimeout(setIntervalId);
								resolve(receiveDataStr);
							}
						}
						let setIntervalId = setTimeout(function() {
							reject("单片机接收超时");
						}, 2000);
					}, function(msg) {
						reject(msg);
					}, dataStr);
				}).then(function(receiveDataStr) {
					console.log("receiveDataStr=" + receiveDataStr);
					that.resolveCommandData(receiveDataStr);
				}, function(err) {
					that.$f7.toast.create({
						text: err,
						position: 'center',
						closeTimeout: 3000,
					}).open();
				});
			},
			sendDataToSinglechip(dataStr, isConfirm) {
				let that = this;

				let send = new Promise(function(resolve, reject) {
					if(!bluetoothStore.enableBluetooth) {
						reject("未打开蓝牙");
						return;
					}
					if(!dataStr) {
						reject("发送内容不能为空");
						return;
					}
					if(!bluetoothStore.connectedDevice) {
						reject("当前未连接到蓝牙设备");
						return;
					}

					cxg.bluetoothSerial.sendData(function() {
						if(!isConfirm) {
							resolve();
							return;
						}
						let startTime = new Date().getTime();
						let setIntervalId = setInterval(function() {
							let currentTime = new Date().getTime();
							if((currentTime - startTime) > 2000) {
								clearInterval(setIntervalId);
								reject("单片机接收超时");
							}
							console.log(bluetoothStore.receiveDataStr);
							if(bluetoothStore.receiveDataStr == dataStr) {
								resolve();
							}
						}, 30);
					}, function(msg) {
						reject(msg);
					}, dataStr);
				});
				return send;
			},
			syncCarSetting() {

			},
			//解析指令
			//指令格式 "[指令名:指令参数1&指令参数2&指令参数3]"
			resolveCommandData(comdata) {
				let that = this;
				let si = comdata.indexOf('['); //指令开始字符
				let ei = comdata.indexOf(']'); //指令结束字符

				if(!(si > -1 && ei > -1 && ei > si)) {
					return;
				}

				comdata = comdata.substring(si + 1, ei);
				let commandIndex = comdata.indexOf(':');
				if(commandIndex <= 1) {
					return;
				}

				//指令名
				let commandName = comdata.substring(0, commandIndex);
				//指令内容
				let commandBody = comdata.substring(commandIndex + 1);

				let paramArr = commandBody.split("&");
				let paramObj = {};
				let keyStr, valueStr;
				paramArr.forEach(function(item, index) {
					let i = item.indexOf("=");
					if(i > -1) {
						keyStr = item.substring(0, i);
						valueStr = item.substring(i + 1);
						paramObj[keyStr] = valueStr;
					}
				})

				if(commandName === "r-p") {
					that.resolveReadParams(paramObj);
					return;
				}
			},

			//手动遥控控制指令
			//指令例子: [mc:200&20] , [mc:-150&-30]
			resolveReadParams(paramObj) {
				if(typeof paramObj["cm"] !== 'undefined') {
					this.carSetting.carMode = paramObj["cm"];
				}
				if(typeof paramObj["atTMD"] !== 'undefined') {
					this.carSetting.atTurnMaxDeg = paramObj["atTMD"] * 1;
					this.carSetting.oldAtTurnMaxDeg = this.carSetting.atTurnMaxDeg;
				}
				if(typeof paramObj["atMS"] !== 'undefined') {
					this.carSetting.atMaxSpeed = paramObj["atMS"] * 1;
					this.carSetting.oldAtMaxSpeed = this.carSetting.atMaxSpeed;
				}
			},
		},
		components: {
			Bluetooth,
			Rocker
		}
	}
</script>

<style scoped>

</style>