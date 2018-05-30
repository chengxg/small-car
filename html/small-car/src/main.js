import Vue from 'vue';

import Framework7 from 'framework7/dist/framework7.esm.bundle.js';
import Framework7Vue from 'framework7-vue/dist/framework7-vue.esm.bundle.js';

import Framework7Styles from 'framework7/dist/css/framework7.css';
import IconsStyles from './assets/framework7/css/icons.css';
import AppStyles from './assets/framework7/css/app.css';
import fontAwesome from './assets/font-awesome/4.7.0/css/font-awesome.css';

import Routes from './routes.js'
import App from './app';

Vue.use(Framework7Vue, Framework7)

let vm = new Vue({
	el: '#app',
	template: '<app/>',
	framework7: {
		id: 'io.framework7.testapp', // App bundle ID
		name: 'Framework7', // App name
		theme: 'md', // Automatic theme detection
		routes: Routes,
	},
	components: {
		app: App
	}
});


document.addEventListener('deviceready', function() {
	let lastTime = 0;
	
	document.addEventListener("backbutton", function() {
		let nowTime = new Date().getTime();
		if(nowTime - lastTime < 600){
			navigator.app.exitApp();
			return;
		}
		lastTime = nowTime;
		vm.$f7.router.back();
	}, false);

}, false);