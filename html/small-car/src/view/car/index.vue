<template>
	<div id="carPage">
		<mt-header fixed title="蓝牙小车" class="" style="background-color: #f7f7f7;color: black;">
			<router-link to="/" slot="left">
				<mt-button icon="back">返回</mt-button>
			</router-link>
		</mt-header>

		<div class="mint-content">
			<ul class="w3-ul">
				<li class="w3-item-middle">
					<div style="width: calc(100% - 80px);" v-if="bluetoothStore.connectedDevice">
						<i class="fa fa-bluetooth fa-2x w3-text-blue" aria-hidden="true"></i>&nbsp;
						<span class="w3-large">{{bluetoothStore.connectedDevice.name}}</span>
					</div>
					<div style="width: calc(100% - 80px);" v-else>
						<span class="w3-medium">请先连接蓝牙设备</span>
					</div>
					<div style="width: 80px;">
						<button @click="openBluetoothDialog" type="button" class="mint-button mint-button--default mint-button--small is-plain" style="">
							<span>蓝牙管理</span>
						</button>
					</div>
				</li>
			</ul>

			<mt-radio align="left" title="小车模式" v-model="carSetting.carMode" :options="options">
			</mt-radio>
			<button @click="setCarMode" type="button" class="mint-button mint-button--default mint-button--small is-plain" style="">
				<span>设置小车模式</span>
			</button>

			小车最大寻迹速度:
			<mt-range v-model="carSetting.traceMaxForwardSpeed" :min="50" :max="255" :step="5" :bar-height="5">
			</mt-range>

			小车转弯角度:
			<mt-range v-model="carSetting.turnMaxDeg" :min="10" :max="60" :step="1" :bar-height="5">
			</mt-range>

			<BluetoothManage :show="isShowBluetoothDialog" :beforeClose="closeBluetoothDialog"></BluetoothManage>
			<Rocker :ballPos="ballPos" :autoMove="true"></Rocker>
			{{ballPos}} {{msg}} 
			<br>receiveDataStr:{{bluetoothStore.receiveDataStr}}
		</div>
	</div>
</template>

<script>
	import BluetoothManage from '@/components/BluetoothManage'
	import Rocker from '@/components/Rocker'
	import { bluetoothStore } from '@/store';

	export default {
		name: 'CarPage',
		data: function() {
			return {
				isShowBluetoothDialog: false,
				bluetoothStore: bluetoothStore,
				ballPos: {
					x: 0,
					y: 0
				},
				rockerWidth: 0,
				options: [{
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
				msg: "",
				carSetting: {
					carMode: "1",
					traceMaxForwardSpeed: 100,
					turnMaxDeg: 30
				}
			}
		},
		created: function() {
			let rockerWidth = window.screen.width < window.screen.height ? window.screen.width : window.screen.height;
			this.rockerWidth = rockerWidth;

			let that = this;
			setInterval(function() {
				that.sendData();
			}, 100);
			bluetoothStore.receiveDataCallback = this.receiveDataCallback;
		},
		watch: {
			"carSetting.traceMaxForwardSpeed": function() {
				this.setTraceMaxSpeed();
			},
			"carSetting.turnMaxDeg": function() {
				this.setTurnMaxDeg();
			}
		},
		methods: {
			openBluetoothDialog: function() {
				this.isShowBluetoothDialog = true;
			},
			closeBluetoothDialog: function() {
				this.isShowBluetoothDialog = false;
			},
			sendData: function() {
				if(this.carSetting.carMode == "2") {
					return;
				}
				let r = Math.sqrt(this.ballPos.x * this.ballPos.x + this.ballPos.y * this.ballPos.y);
				let speedDir = 1;
				if(this.ballPos.y > 0) {
					speedDir = -1;
				}
				let degDir = 1;
				if(this.ballPos.x < 0) {
					degDir = -1;
				}

				let speed = speedDir * Math.round(2 * r / this.rockerWidth * 255);
				//let servoDeg = Math.round(this.ballPos.x / (this.rockerWidth / 2) * 40);
				let servoDeg = 0;
				if(this.ballPos.y != 0) {
					servoDeg = Math.atan(Math.abs(this.ballPos.x / this.ballPos.y)) / Math.PI * 180;
				}

				servoDeg = degDir * Math.ceil(servoDeg / 90 * 50);
				this.bluetoothStore.sendDataFunc("[mc:" + speed + "&" + servoDeg + "]");
				this.msg = "[mc:" + speed + "&" + servoDeg + "]";
			},
			readData: function() {

			},
			receiveDataCallback: function(data) {
				console.log(data);
				bluetoothStore.receiveDataStr = data;
			},
			setCarMode() {
				this.bluetoothStore.sendDataFunc("[cm:" + this.carSetting.carMode + "]");
			},
			setTraceMaxSpeed(speed) {
				this.bluetoothStore.sendDataFunc("[tms:" + this.carSetting.traceMaxForwardSpeed + "]");
			},
			setTurnMaxDeg() {
				this.bluetoothStore.sendDataFunc("[tmd:" + this.carSetting.turnMaxDeg + "]");
			}
		},
		components: {
			BluetoothManage,
			Rocker
		}
	}
</script>

<style scoped>
	.mint-content { padding: 40px 5px 5px 5px;background: #EFEFF5;}
	.w3-ul { margin-top: 5px; background: white;}
</style>