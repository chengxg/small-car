// The Vue build version to load with the `import` command
// (runtime-only or standalone) has been set in webpack.base.conf with an alias.
import Vue from 'vue'
import App from './App'
import router from './router'
import bluetoothPlugin from './assets/js/plugin/bluetooth'

import './assets/font-awesome/4.7.0/css/font-awesome.css'
import './assets/mui/css/mui.css'
import './assets/w3-css/w3-4.css'
import './assets/w3-css/w3-extension.css'

import MintUI from 'mint-ui'
import 'mint-ui/lib/style.css'
Vue.use(MintUI)

Vue.config.productionTip = false

/* eslint-disable no-new */
new Vue({
  el: '#app',
  router,
  components: { App },
  template: '<App/>'
})

//初始化插件
document.addEventListener("plusready", function() {
  window.plus.bluetooth = bluetoothPlugin;
  bluetoothPlugin.init();
},false);
