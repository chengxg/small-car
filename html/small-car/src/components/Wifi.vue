<template>
	<f7-page>
		<f7-navbar title="tcp连接">
			<f7-nav-right>
				<f7-link popup-close>x</f7-link>
			</f7-nav-right>
		</f7-navbar>

		<f7-block>
			IP地址: <input type="text" v-model="wifiStore.ipAddress"/><br>
			端口: <input type="text" v-model="wifiStore.port"/><br>
			<button type="button" class="button button-fill button-raised button-round" @click="connectToTcpServer">连接</button><br>
			<br><br>
			<button type="button" class="button button-fill button-raised button-round" @click="disconnectTcpServer">断开连接</button><br>
			
			
			<textarea cols="20" rows="5" v-model="sendDataStr"></textarea><br>
			<button type="button" class="button button-fill button-raised button-round" @click="sendData">发送数据</button><br>
			
			readDataStr:{{readDataStr}}<br><br>
			successMsg:{{successMsg}}<br>
			errMsg:{{errMsg}}<br>
		</f7-block>
	</f7-page>

</template>

<script>
	import { wifiStore } from '@/store';

	export default {
		name: 'WifiTcp',
		data: function() {
			return {
				wifiStore:wifiStore,
				sendDataStr:"",
				readDataStr:"",
				successMsg:"",
				errMsg:""
			}
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
				
			},
			connectToTcpServer(){
				let that = this;
				cxg.wifiManage.connectToTcpServer(function(msg) {
					that.successMsg = msg;
					that.readData();
				}, function(msg) {
					that.errMsg = msg;
				},this.wifiStore.ipAddress,this.wifiStore.port);
			},
			disconnectTcpServer(){
				cxg.wifiManage.disconnectTcpServer(function(msg) {
					that.successMsg = msg;
				}, function(msg) {
					that.errMsg = msg;
				});
			},
			readData(){
				let that = this;
				cxg.wifiManage.readData(function(dataStr) {
					that.readDataStr = dataStr;
				}, function(msg) {
					that.errMsg = msg;
				},128);
			},
			sendData(){
				let that = this;
				cxg.wifiManage.sendData(function(msg) {
					that.successMsg = msg;
				}, function(msg) {
					that.errMsg = msg;
				},this.sendDataStr);
			}
		},
		components: {

		}
	}
</script>

<style scoped>

</style>