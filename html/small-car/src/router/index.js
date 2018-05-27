import Vue from "vue";
import Router from "vue-router";
import Rocker from "@/components/Rocker";
import CarPage from "@/view/car/index";
import CarPage2 from "@/view/car/index2";

Vue.use(Router);

export default new Router({
  routes: [
/*     {
      path: "/",
      name: "HelloWorld",
      component: HelloWorld,
      children: [
        {
          path: "sencond",
          component: sencond
        }
      ]
    }, */
    {
      path: "/rocker",
      name: "Rocker",
      component: Rocker
    },
    {
      path: "/",
      name: "CarPage2",
      component: CarPage2
    },
    {
      path: "/car",
      name: "CarPage",
      component: CarPage
    }
  ]
});
