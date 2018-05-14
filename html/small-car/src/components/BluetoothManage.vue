<template>
    <MyDialog :show="show" :beforeClose="beforeClose">
        <h4 slot="header">蓝牙连接</h4>
        <div class="content">
        	
            <ul class="w3-ul">
                <li class="w3-item-middle">
                    <div style="width: 80%;"><span class="w3-large">开启蓝牙：</span></div>
                    <div style="width: 20%;">
                    	<div class="w3-switch w3-switch-blue" :class="{'w3-active':enableBluetooth}" @click="turnOnOrOffBluetooth">
						  <div class="w3-switch-handle"></div>
						</div>
                    </div>/
                </li>
            </ul>

            <h5 class="w3-margin-top">已配对的设备:</h5>
            <ul class="w3-ul">
                 <li class="w3-item-middle w3-ripple" v-for="(device,index) in pairedDevices" :key="index" @click="connDevice(device)">
            		<div class="" style="width:30px;">
                		<i v-if="connectingDevice === device" class="fa fa-spinner fa-spin fa-2x w3-text-blue" aria-hidden="true"></i>
                		<i v-else class="fa fa-bluetooth fa-2x" :class="{'w3-text-blue':connectedDevice === device}" aria-hidden="true"></i>
                    </div>
                    <div class="w3-text-ellipsis" style="width: calc(100% - 150px);">
                    	<span class="w3-large">{{device.name}}</span>
                    </div>
                    <div class="w3-text-grey" style="width:120px;">
                        <span class="w3-medium">{{device.address}}</span>
                    </div>
                </li>
                <!--<li class="w3-item-middle" >
            		<div class="" style="width:30px;">
                		<i class="fa fa-bluetooth fa-2x w3-text-blue" aria-hidden="true"></i>
                    </div>
                    <div class="w3-text-ellipsis" style="width: calc(100% - 150px);">
                    	<span class="w3-large">namenamenamenamenamename</span>
                    </div>
                    <div class="w3-text-grey" style="width:120px;">
                        <span class="w3-medium">10.EF.12.ac.45:E7</span>
                    </div>
                </li>-->
                <li class="w3-item-middle" v-if="pairedDevices.length === 0">
                     <span>未获取到已配对的设备</span>
                </li>
            </ul>

            <h5 class="w3-margin-top">已发现的设备:</h5>
            <ul class="w3-ul">
                <li class="w3-item-middle" v-for="(device,index) in newDevices" :key="index" @click="connDevice(device)">
                    <div class="" style="width:30px;">
                		<i v-if="connectingDevice === device" class="fa fa-spinner fa-spin fa-2x w3-text-blue" aria-hidden="true"></i>
                		<i v-else class="fa fa-bluetooth fa-2x" :class="{'w3-text-blue':connectedDevice === device}" aria-hidden="true"></i>
                    </div>
                    <div class="w3-text-ellipsis" style="width: calc(100% - 150px);">
                    	<span class="w3-large">{{device.name}}</span>
                    </div>
                    <div class="w3-text-grey" style="width:120px;">
                        <span class="w3-medium">{{device.address}}</span>
                    </div>
                </li>
                <li class="w3-item-middle w3-medium" v-if="newDevices.length === 0" >
                    <span>未发现新的设备</span>
                </li>
            </ul>
            
            <button v-if="!isFindding" @click="findDevices" type="button" class="mint-button mint-button--default mint-button--large is-plain w3-margin-top" style="">
                <span>搜索蓝牙设备</span>
            </button>
            <button v-if="isFindding" type="button" class="mint-button mint-button--default mint-button--large is-plain w3-margin-top" style="">
                <i class="fa fa-spinner fa-spin" aria-hidden="true"></i>&nbsp;
                <span>正在搜索...</span>
            </button>
        </div>
    </MyDialog>
</template>

<script>
import { Toast } from 'mint-ui';
import MyDialog from './MyDialog';
import {bluetoothStore} from '@/store';

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
        	Toast({
			  message: this.successMsg,
			  position: 'bottom',
			  duration: 2000
			});
        },
        "errMsg": function() {
        	Toast({
			  message: this.errMsg,
			  position: 'bottom',
			  duration: 3000
			});
        }
    },
    methods: {
        init() {
            this.enableBluetooth = plus.bluetooth.getBluetoothStatus();
            this.getPairedDevices();
            this.listenBluetoothStatus();
        },
        listenBluetoothStatus: function() {
            let that = this;
            plus.bluetooth.listenBluetoothStatus(function(state) {
                switch (state) {
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
        turnOnOrOffBluetooth: function() {
            this.turnOnBluetooth();
            this.turnOffBluetooth();
        },
        turnOnBluetooth: function() {
            if (this.enableBluetooth) {
                return;
            }

            let that = this;
            plus.bluetooth.turnOnBluetooth(function(msg) {
                that.successMsg = msg;
            }, function(msg) {
                that.errMsg = msg;
            });
        },
        turnOffBluetooth: function() {
            if (!this.enableBluetooth) {
                return;
            }
            let that = this;
            plus.bluetooth.turnOffBluetooth(function(msg) {
                that.successMsg = msg;
                that.isFindding = false;
                //that.enableBluetooth = false;
            }, function(msg) {
                that.errMsg = msg;
            });
        },
        getPairedDevices: function() {
            let that = this;
            plus.bluetooth.getPairedDevices(function(devices) {
            	devices.forEach(function(item){
            		item.connectingDevice = null;
            	});
                that.pairedDevices = devices;
            }, function(msg) {
                that.errMsg = msg;
            });
        },
        findDevices: function() {
            let that = this;
            if (!this.enableBluetooth) {
                return;
            }
            if (this.isFindding) {
                return;
            }
            this.isFindding = true;
            that.newDevices = [];
            plus.bluetooth.findingNewDevices(function(data) {
                if (typeof data === 'string') {
                    that.isFindding = false;
                }
                if (typeof data === 'object') {
                    that.newDevices.push(data);
                }
            }, function(msg) {
                that.errMsg = msg;
                that.isFindding = false;
            });
        },
        connDevice: function(device) {
            let that = this;
            if (!this.enableBluetooth) {
                return;
            }
            if (!device) {
                that.errMsg = "请输入蓝牙设备地址!";
                return;
            }
            if(this.connectedDevice === device){
            	that.disConnDevice();
            	return;
            }
            that.connectingDevice = device;
            plus.bluetooth.connDevice(function(msg) {
                that.connectingDevice = null;
                that.successMsg = msg;
                that.readData();
                that.connectedDevice = device;
            }, function(msg) {
                that.errMsg = msg;
                that.connectingDevice = null;
            }, device.address);
        },
        disConnDevice:function(){
        	if(this.connectedDevice){
        		let that = this;
        		 that.connectingDevice = that.connectedDevice;
        		 plus.bluetooth.disConnDevice(function(msg) {
	                that.connectedDevice = null;
	                that.connectingDevice = null;
	            }, function(msg) {
	                that.errMsg = msg;
	                that.connectingDevice = null;
	            });
        	}
        },
        readData: function() {
            if (!this.enableBluetooth) {
                return;
            }

            let that = this;
            plus.bluetooth.readData(bluetoothStore.receiveDataCallback, function(msg) {
            	that.connectedDevice = null;
                that.errMsg = msg;
            }, this.bufferSize);
        }
    },
    components: {
        MyDialog
    }
}
</script>

<style scoped>
    .content{
    	padding: 5px 10px;
    	background: #EFEFF5;
    }
    .w3-ul{
    	margin-top: 5px;
    	background: white;
    }
</style>
