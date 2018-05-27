import HomePage from './pages/home.vue';
import AboutPage from './pages/about.vue';
import NotFoundPage from './pages/not-found.vue';
import BluetoothCar from './view/car/bluetooth-car.vue';

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
		path: '(.*)',
		component: NotFoundPage,
	},
];