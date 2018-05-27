// Import Vue
import Vue from 'vue';

// Import F7
import Framework7 from 'framework7/dist/framework7.esm.bundle.js';
// Import F7 Vue Plugin
import Framework7Vue from 'framework7-vue/dist/framework7-vue.esm.bundle.js';
// Import F7 Styles
import Framework7Styles from 'framework7/dist/css/framework7.css';

// Import Icons and App Custom Styles
import IconsStyles from './assets/framework7/css/icons.css';
import AppStyles from './assets/framework7/css/app.css';
import font from './assets/font-awesome/4.7.0/css/font-awesome.css';

// Import Routes
import Routes from './routes.js'

// Import App Component
import App from './app';

// Init F7 Vue Plugin
Vue.use(Framework7Vue, Framework7)

// Init App
let vm = new Vue({
	el: '#app',
	template: '<app/>',
	// Init Framework7 by passing parameters here
	framework7: {
		id: 'io.framework7.testapp', // App bundle ID
		name: 'Framework7', // App name
		theme: 'md', // Automatic theme detection
		// App routes
		routes: Routes,
	},
	// Register App Component
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
		/*vm.$f7.toast.create({
			text: "再按一次退出程序",
			position: 'center',
			closeTimeout: 3000,
		}).open();*/
		vm.$f7.router.back();
	}, false);

}, false);