// Import Vue
import Vue from 'vue';
import Vuetify from 'vuetify'

Vue.use(Vuetify)
// Import Routes
import Routes from './routes.js'
// index.js or main.js
import 'material-design-icons-iconfont/dist/material-design-icons.css'
import 'vuetify/dist/vuetify.min.css'

import font from './assets/font-awesome/4.7.0/css/font-awesome.css';

// Import App Component
import App from './app';

// Init App
new Vue({
  el: '#app',
  template: '<app/>',
  components: {
    app: App
  }
});


