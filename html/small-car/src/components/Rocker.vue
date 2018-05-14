<template>
	<div id="rocker" class="rocker">

		<div id="rockerBall" class="rocker-ball" :style="rockerBallStyle">

		</div>

	</div>
</template>

<script>
	import { Toast } from 'mint-ui';

	let rocker = {
		$el: null,
		$ball: null,
		$rockerRadius: 0, //摇杆半径
		ballPosX: 0,
		ballPosY: 0,
		startMoveX: 0,
		startMoveY: 0,
		evtStartX: 0,
		evtStartY: 0,
		autoMoveAnimationId: 0,
		isAutoMoveToOrign: true,

		ballMove: function(e) {
			e.preventDefault(); //阻止触摸时浏览器的缩放、滚动条滚动等
			let touch = e.touches && e.touches[0]; //获取第一个触点
			let x = e.pageX || touch.pageX;
			let y = e.pageY || touch.pageY;
			let ballPosX = Math.round(x - rocker.evtStartX + rocker.startMoveX);
			let ballPosY = Math.round(y - rocker.evtStartY + rocker.startMoveY);
			if(!rocker.$isInnerCircle(ballPosX, ballPosY)) {
				let len = Math.sqrt(ballPosX * ballPosX + ballPosY * ballPosY);
				ballPosX = Math.round(rocker.$rockerRadius * ballPosX / len);
				ballPosY = Math.round(rocker.$rockerRadius * ballPosY / len);
			}

			rocker.ballPosX = ballPosX;
			rocker.ballPosY = ballPosY;
			rocker.ballMoveTo();
		},
		$isInnerCircle: function(x, y) {
			if((x * x + y * y) > rocker.$rockerRadius * rocker.$rockerRadius) {
				return false;
			}
			return true;
		},
		ballMoveTo: function() {
			let posTransform = "translate(" + rocker.ballPosX + "px," + rocker.ballPosY + "px)";
			rocker.$ball.style.transform = posTransform;
		},
		clearAutoMoveAnimation: function() {
			if(rocker.autoMoveAnimationId) {
				cancelAnimationFrame(rocker.autoMoveAnimationId);
			}
		},
		autoMoveToOrignAnimation: function() {
			rocker.clearAutoMoveAnimation();

			function move() {
				if(Math.abs(rocker.ballPosX) <= 10) {
					rocker.ballPosX = 0;
				} else {
					rocker.ballPosX = Math.round(rocker.ballPosX - 10 * rocker.ballPosX / Math.abs(rocker.ballPosX));
				}
				if(Math.abs(rocker.ballPosY) <= 10) {
					rocker.ballPosY = 0;
				} else {
					rocker.ballPosY = Math.round(rocker.ballPosY - 10 * rocker.ballPosY / Math.abs(rocker.ballPosY));
				}
				rocker.startMoveX = rocker.ballPosX;
				rocker.startMoveY = rocker.ballPosY;
				rocker.ballMoveTo();
				if(rocker.ballPosX === 0 && rocker.ballPosY === 0) {
					rocker.clearAutoMoveAnimation();
				} else {
					rocker.autoMoveAnimationId = requestAnimationFrame(move);
				}
			}
			move();
		},
		touchstart: function(e) {
			let target = e.target;
			if(target === rocker.$ball) {
				e.preventDefault(); //阻止触摸时浏览器的缩放、滚动条滚动等

				rocker.clearAutoMoveAnimation();
				let touch = e.touches && e.touches[0]; //获取第一个触点
				rocker.evtStartX = e.pageX || touch.pageX;
				rocker.evtStartY = e.pageY || touch.pageY;

				rocker.startMoveX = rocker.ballPosX;
				rocker.startMoveY = rocker.ballPosY;

				document.addEventListener("touchmove", rocker.ballMove, false);
				document.addEventListener("mousemove", rocker.ballMove, false);
			}
		},
		touchend: function(e) {
			if(rocker.isAutoMoveToOrign) {
				rocker.autoMoveToOrignAnimation(); //自动归0
			}
			document.removeEventListener("touchmove", rocker.ballMove, false);
			document.removeEventListener("mousemove", rocker.ballMove, false);
		},
		init: function() {
			rocker.$el = document.getElementById("rocker");
			rocker.$ball = document.getElementById("rockerBall");
			rocker.$rockerRadius = Math.round(rocker.$el.offsetWidth / 2);

			document.addEventListener("touchstart", rocker.touchstart, false);
			document.addEventListener("touchend", rocker.touchend, false);
			document.addEventListener("mousedown", rocker.touchstart, false);
			document.addEventListener("mouseup", rocker.touchend, false);
		},
		destroyed: function() {
			document.removeEventListener("touchstart", rocker.touchstart, false);
			document.removeEventListener("touchend", rocker.touchend, false);
			document.removeEventListener("mousedown", rocker.touchstart, false);
			document.removeEventListener("mouseup", rocker.touchend, false);
		}
	};

	let aid = null;

	export default {
		name: 'Rocker',
		data() {
			return rocker
		},
		props: {
			"autoMove": {
				type: Boolean,
				default: false
			},
			"ballPos": {
				type: Object,
				default: function() {
					return {
						x: 0,
						y: 0
					}
				}
			}
		},
		created: function() {
			rocker.isAutoMoveToOrign = this.autoMove;
			this.ballPos.x = this.ballPosX;
			this.ballPos.y = this.ballPosY;

			let that = this;
			setTimeout(function() {
				//that.watchAcceleration();
			}, 1000);

		},
		computed: {
			rockerBallStyle: function() {
				return {
					transform: "translate(" + this.ballPosX + "px," + this.ballPosY + "px)"
				}
			}
		},
		watch: {
			"autoMove": function() {
				rocker.isAutoMoveToOrign = this.autoMove;
			},
			"ballPosX": function() {
				this.ballPos.x = this.ballPosX;
			},
			"ballPosY": function() {
				this.ballPos.y = this.ballPosY;
			}
		},
		mounted: function() {
			rocker.init();
		},
		methods: {
			watchAcceleration() {
				if(aid) {
					this.watchAccelerationStop();
					return;
				}
				let that = this;
				let rate = rocker.$rockerRadius / 5.6;
				let t = new Date().getTime();
				
				/*requestAnimationFrame(watch);
				function watch(){
					// 扩展API加载完毕，现在可以正常调用扩展API
					plus.accelerometer.getCurrentAcceleration(function( a ) {
						//console.log(a);
						let now = new Date().getTime();
						console.log(now-t);
						t = now;
						let x = a.xAxis * rate;
						let y = -a.yAxis * rate;
						if(!rocker.$isInnerCircle(x, y)) {
							let len = Math.sqrt(x * x + y * y);
							x = Math.round(rocker.$rockerRadius * x / len);
							y = Math.round(rocker.$rockerRadius * y / len);
						}
	
						rocker.ballPosX = x;
						rocker.ballPosY = y;
						rocker.ballMoveTo();
					});
					requestAnimationFrame(watch);
				}*/
				
				aid = plus.accelerometer.watchAcceleration(function(a) {
					//console.log(a);
					let now = new Date().getTime();
					console.log(now-t);
					t = now;
					let x = a.xAxis * rate;
					let y = -a.yAxis * rate;
					if(!rocker.$isInnerCircle(x, y)) {
						let len = Math.sqrt(x * x + y * y);
						x = Math.round(rocker.$rockerRadius * x / len);
						y = Math.round(rocker.$rockerRadius * y / len);
					}

					rocker.ballPosX = x;
					rocker.ballPosY = y;
					rocker.ballMoveTo();
				}, function(e) {
					Toast({
						message: "监听加速度传感器失败," + e.message,
						position: 'bottom',
						duration: 3000
					});
				},{frequency:20} );
			},
			watchAccelerationStop() {
				if(aid) {
					plus.accelerometer.clearWatch(aid);
					aid = null;
				}
			}
		},
		destroyed: function() {
			rocker.destroyed();
		}
	}
</script>

<style scoped>
	* { touch-action: none;}
	.rocker { position: relative; z-index: 4; display: flex; justify-content: center; align-items: center; border: 1px solid groove; width: 100%; border-radius: 50%; box-sizing: border-box; background: gray;}
	.rocker:before { content: ""; padding-bottom: 100%; width: 0.1px;}
	.rocker-ball { position: absolute; width: 20%; height: 20%; border-radius: 50%; background: red; box-sizing: border-box;}
	.rocker-ball:before { content: ""; padding-bottom: 100%; width: 0.1px; transform: translate();}
</style>