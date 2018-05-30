import HomePage from './view/home.vue';
import AboutPage from './view/about.vue';
import NotFoundPage from './view/not-found.vue';
import BluetoothCar from './view/car/bluetooth-car.vue';
import WifiCar from './view/car/wifi-car.vue';

export default [{
		path: '/',
		component: HomePage,
	},
	{
		path: '/about/',
		component: AboutPage,
	},
	{
		path: '/car/bluetooth/',
		component: BluetoothCar,
	},
	{ 
		path: '/car/wifi/',
		component: WifiCar,
	},
	{
		path: '(.*)',
		component: NotFoundPage,
	},
];