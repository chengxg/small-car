<template>
	<div id="rocker-wrap">

		<div id="rocker">
			<button type="button" class="button button-raised button-fill back-btn" style="" @click="closeRocker">
				<i class="icon icon-back"></i>
			</button>
			<div class="card control-panel">
				<div class="control-panel-item">
					<label class="title">角度自动回归:</label>
					<f7-toggle :checked="autoMoveToOrign.isHBallAutoMove" @change="onChangeHBallAutoMoveToggle" slot="after"></f7-toggle>
				</div>
				<div class="control-panel-item">
					<label class="title">速度自动回归:</label>
					<f7-toggle :checked="autoMoveToOrign.isVBallAutoMove" @change="onChangeVBallAutoMoveToggle" slot="after"></f7-toggle>
				</div>
			</div>
			<div class="card msg-panel">
				<div class="msg-panel-item">
					<label class="title">当前速度:</label>
					<span class="msg">{{speed}}</span>
				</div>
				<div class="msg-panel-item">
					<label class="title">当前角度:</label>
					<span class="msg">{{deg}}</span>
				</div>
			</div>
			<div id="horizontal">
				<div class="control-btn-wrap">
					<button id="degLeftBtn" type="button" class="button button-raised button-fill" style="border-radius: 50% 0 0 50%;transform: translateX(-50%);left:20%;">
						<i class="fa fa-angle-left fa-2x" aria-hidden="true"></i>
					</button>
					
					<button id="degRightBtn" type="button" class="button button-raised button-fill " style="border-radius: 0 50% 50% 0;transform: translateX(-50%);left:80%;">
						<i class="fa fa-angle-right fa-2x" aria-hidden="true"></i>
					</button>
				</div>
				<div class="track">

				</div>
				<div id="hRockerBall" class="rocker-ball">

				</div>
			</div>
			<div id="vertical">
				<div class="control-btn-wrap">
					<button id="speedUpBtn" type="button" class="button button-raised button-fill" style="border-radius: 50% 0 0 50%;transform:rotateZ(90deg)  translateY(-50%);top:30%;">
						<i class="fa fa-angle-left fa-2x" aria-hidden="true"></i>
					</button>
					
					<button id="speedDownBtn" type="button" class="button button-raised button-fill" style="border-radius: 0 50% 50% 0;transform:rotateZ(90deg)  translateY(-50%);top:70%;">
						<i class="fa fa-angle-right fa-2x" aria-hidden="true"></i>
					</button>
				</div>

				<div class="track">

				</div>
				<div id="vRockerBall" class="rocker-ball">

				</div>
			</div>
		</div>
	</div>
</template>

<script>
	import { bluetoothStore } from '@/store';
	import Timer from '@/assets/util/Timer';

	let rocker = {
		$el: null,
		$hRockerBall: null,
		$vRockerBall: null,
		maxX: 0,
		maxY: 0,
		isAutoMoveToOrign: true,
		sendCommandIntervalId: 0,
		hBallPos: {
			x: 0,
			y: 0
		},
		vBallPos: {
			x: 0,
			y: 0
		},
		hBallStartPos: {
			x: 0,
			y: 0
		},
		vBallStartPos: {
			x: 0,
			y: 0
		},

		hEvtLastPos: {
			x: 0,
			y: 0
		},
		vEvtLastPos: {
			x: 0,
			y: 0
		},

		hEvtStartPos: {
			x: 0,
			y: 0
		},
		vEvtStartPos: {
			x: 0,
			y: 0
		},

		touchmove: function(e) {
			e.preventDefault(); //阻止触摸时浏览器的缩放、滚动条滚动等
			let target = e.target;
			if(!(target === rocker.$hRockerBall || target === rocker.$vRockerBall)) {
				return;
			}

			let posDiff = 100;

			Array.prototype.slice.call(e.touches).forEach(function(touch) {
				let evtX = touch.pageX;
				let evtY = touch.pageY;
				if(Math.abs(evtX - rocker.hEvtLastPos.x) < posDiff && Math.abs(evtY - rocker.hEvtLastPos.y) < posDiff) {

					rocker.hBallPos.y = Math.round(evtY - rocker.hEvtStartPos.y + rocker.hBallStartPos.y);
					rocker.hEvtLastPos = {
						x: evtX,
						y: evtY
					};
					if(rocker.maxX < Math.abs(rocker.hBallPos.y)) {
						rocker.hBallPos.y = rocker.maxX * rocker.hBallPos.y / Math.abs(rocker.hBallPos.y);
					}
				}
				if(Math.abs(evtX - rocker.vEvtLastPos.x) < posDiff && Math.abs(evtY - rocker.vEvtLastPos.y) < posDiff) {
					rocker.vBallPos.x = Math.round(evtX - rocker.vEvtStartPos.x + rocker.vBallStartPos.x);
					rocker.vEvtLastPos = {
						x: evtX,
						y: evtY
					};
					if(rocker.maxY < Math.abs(rocker.vBallPos.x)) {
						rocker.vBallPos.x = rocker.maxY * rocker.vBallPos.x / Math.abs(rocker.vBallPos.x);
					}
				}
			})
			rocker.$setMoveBall();
		},
		$setMoveBall: function() {
			requestAnimationFrame(function() {
				if(rocker.maxX < Math.abs(rocker.hBallPos.y)) {
					rocker.hBallPos.y = rocker.maxX * rocker.hBallPos.y / Math.abs(rocker.hBallPos.y);
				}
				rocker.$hRockerBall.style.transform = "translate(" + rocker.hBallPos.y + "px,0px)";

				if(rocker.maxY < Math.abs(rocker.vBallPos.x)) {
					rocker.vBallPos.x = rocker.maxY * rocker.vBallPos.x / Math.abs(rocker.vBallPos.x);
				}

				rocker.$vRockerBall.style.transform = "translate(0px," + (-rocker.vBallPos.x) + "px)";
			});
		},
		touchstart: function(e) {
			e.preventDefault(); //阻止触摸时浏览器的缩放、滚动条滚动等
			let target = e.target;
			if(!(target === rocker.$hRockerBall || target === rocker.$vRockerBall)) {
				return;
			}

			let hx = rocker.$hRockerBall.getBoundingClientRect().left;
			let hy = rocker.$hRockerBall.getBoundingClientRect().top;
			let vx = rocker.$vRockerBall.getBoundingClientRect().left;
			let vy = rocker.$vRockerBall.getBoundingClientRect().top;

			let posDiff = 100;
			rocker.hEvtStartPos = {
				x: 0,
				y: 0
			};
			rocker.hEvtLastPos = rocker.hEvtStartPos;
			rocker.vEvtStartPos = {
				x: 0,
				y: 0
			};
			rocker.vEvtLastPos = {
				x: 10000,
				y: 10000
			};
			Array.prototype.slice.call(e.touches).forEach(function(touch) {
				let evtX = touch.pageX;
				let evtY = touch.pageY;

				if(Math.abs(evtX - hx) < posDiff && Math.abs(evtY - hy) < posDiff) {
					rocker.hEvtStartPos = {
						x: evtX,
						y: evtY
					}
					rocker.hBallStartPos = {
						x: rocker.hBallPos.x,
						y: rocker.hBallPos.y
					}
					rocker.hEvtLastPos = rocker.hEvtStartPos;
					rocker.animation.clearHBallAutoMoveToOrign();
				}
				if(Math.abs(evtX - vx) < posDiff && Math.abs(evtY - vy) < posDiff) {
					rocker.vEvtStartPos = {
						x: evtX,
						y: evtY
					}
					rocker.vBallStartPos = {
						x: rocker.vBallPos.x,
						y: rocker.vBallPos.y
					}
					rocker.vEvtLastPos = rocker.vEvtStartPos;
					rocker.animation.clearVBallAutoMoveToOrign();
				}
			})

			document.addEventListener("touchmove", rocker.touchmove, false);
			//rocker.$el.addEventListener("touchmove", rocker.touchmove, false);
			rocker.$setMoveBall();

		},
		touchend: function(e) {
			e.preventDefault(); //阻止触摸时浏览器的缩放、滚动条滚动等
			let target = e.target;

			if(target === rocker.$hRockerBall) {
				rocker.animation.hBallAutoMoveToOrign();
			}
			if(target === rocker.$vRockerBall) {
				rocker.animation.vBallAutoMoveToOrign();
			}

			//document.removeEventListener("touchmove", rocker.touchmove, false);
			//rocker.$el.removeEventListener("touchmove", rocker.touchmove, false);
		},
		init: function() {
			rocker.$el = document.getElementById("rocker");
			rocker.$hRockerBall = document.getElementById("hRockerBall");
			rocker.$vRockerBall = document.getElementById("vRockerBall");

			document.addEventListener("touchstart", rocker.touchstart, false);
			document.addEventListener("touchend", rocker.touchend, false);

			clearInterval(rocker.sendCommandIntervalId);
			rocker.sendCommandIntervalId = setInterval(function() {
				manualContol();
			}, 100);
			rocker.pressControlBtn.init();
		},
		destroyed: function() {
			document.removeEventListener("touchstart", rocker.touchstart, false);
			document.removeEventListener("touchend", rocker.touchend, false);

			clearInterval(rocker.sendCommandIntervalId);
			rocker.animation.clearHBallAutoMoveToOrign();
			rocker.animation.clearVBallAutoMoveToOrign();
		},
		animation: {
			isHBallAutoMove: true,
			isVBallAutoMove: false,
			hBallAutoMoveToOrignId: 0,
			vBallAutoMoveToOrignId: 0,
			clearHBallAutoMoveToOrign: function() {
				if(rocker.animation.hBallAutoMoveToOrignId) {
					cancelAnimationFrame(rocker.animation.hBallAutoMoveToOrignId);
				}
			},
			clearVBallAutoMoveToOrign: function() {
				if(rocker.animation.vBallAutoMoveToOrignId) {
					cancelAnimationFrame(rocker.animation.vBallAutoMoveToOrignId);
				}
			},
			hBallAutoMoveToOrign: function() {
				if(!rocker.animation.isHBallAutoMove) {
					return;
				}
				rocker.animation.clearHBallAutoMoveToOrign();
				move();

				function move() {
					if(Math.abs(rocker.hBallPos.y) <= 10) {
						rocker.hBallPos.y = 0;
					} else {
						rocker.hBallPos.y = Math.round(rocker.hBallPos.y - 10 * rocker.hBallPos.y / Math.abs(rocker.hBallPos.y));
					}
					rocker.$setMoveBall();
					if(rocker.hBallPos.y === 0) {
						rocker.animation.clearHBallAutoMoveToOrign();
					} else {
						rocker.hBallAutoMoveToOrignId = requestAnimationFrame(move);
					}
				}
			},
			vBallAutoMoveToOrign: function() {
				if(!rocker.animation.isVBallAutoMove) {
					return;
				}
				rocker.animation.clearVBallAutoMoveToOrign();
				move();

				function move() {
					if(Math.abs(rocker.vBallPos.x) <= 10) {
						rocker.vBallPos.x = 0;
					} else {
						rocker.vBallPos.x = Math.round(rocker.vBallPos.x - 10 * rocker.vBallPos.x / Math.abs(rocker.vBallPos.x));
					}

					rocker.$setMoveBall();
					if(rocker.vBallPos.x === 0) {
						rocker.animation.clearVBallAutoMoveToOrign();
					} else {
						rocker.animation.vBallAutoMoveToOrignId = requestAnimationFrame(move);
					}
				}
			},
		},
		pressControlBtn: {
			degLeftBtnTime: new Timer("degLeftBtn"),
			degRightBtnTime: new Timer("degRightBtn"),
			speedUpBtnTime: new Timer("speedUpBtn"),
			speedDownBtnTime: new Timer("speedDownBtn"),
			hBallAutoMoveTimeId:0,
			vBallAutoMoveTimeId:0,
			init: function() {
				let degLeftBtn = document.getElementById("degLeftBtn");
				let degRightBtn = document.getElementById("degRightBtn");
				let speedUpBtn = document.getElementById("speedUpBtn");
				let speedDownBtn = document.getElementById("speedDownBtn");

				degLeftBtn.addEventListener("touchstart", rocker.pressControlBtn.pressDegLeftBtn, false);
				degRightBtn.addEventListener("touchstart", rocker.pressControlBtn.pressDegRightBtn, false);
				speedUpBtn.addEventListener("touchstart", rocker.pressControlBtn.pressSpeedUpBtn, false);
				speedDownBtn.addEventListener("touchstart", rocker.pressControlBtn.pressSpeedDownBtn, false);

				degLeftBtn.addEventListener("touchend", rocker.pressControlBtn.pressEndDegLeftBtn, false);
				degRightBtn.addEventListener("touchend", rocker.pressControlBtn.pressEndDegRightBtn, false);
				speedUpBtn.addEventListener("touchend", rocker.pressControlBtn.pressEndSpeedUpBtn, false);
				speedDownBtn.addEventListener("touchend", rocker.pressControlBtn.pressEndSpeedDownBtn, false);
			},
			pressDegLeftBtn: function() {
				clearTimeout(rocker.pressControlBtn.hBallAutoMoveTimeId);
				rocker.animation.clearHBallAutoMoveToOrign();
				rocker.pressControlBtn.degLeftBtnTime.init({
					currentCount: 0,
					repeatCount: 1000000,
					intervalTime: 20,
					delayTime: 300,
					timingFun: function() {
						rocker.hBallPos.y = rocker.hBallPos.y - 6;
						rocker.$setMoveBall();
					},
				}).start();
				rocker.hBallPos.y = rocker.hBallPos.y - 20;
				rocker.$setMoveBall();
			},
			pressEndDegLeftBtn: function() {
				rocker.pressControlBtn.hBallAutoMoveTimeId = setTimeout(function(){
					rocker.animation.hBallAutoMoveToOrign();
				},300);
				rocker.pressControlBtn.degLeftBtnTime.clear();
			},
			pressDegRightBtn: function() {
				clearTimeout(rocker.pressControlBtn.hBallAutoMoveTimeId);
				
				rocker.animation.clearHBallAutoMoveToOrign();
				rocker.pressControlBtn.degRightBtnTime.init({
					currentCount: 0,
					repeatCount: 1000000,
					intervalTime: 20,
					delayTime: 300,
					timingFun: function() {
						rocker.hBallPos.y = rocker.hBallPos.y + 6;
						rocker.$setMoveBall();
					},
				}).start();
				rocker.hBallPos.y = rocker.hBallPos.y + 20;
				rocker.$setMoveBall();
			},
			pressEndDegRightBtn: function() {
				rocker.pressControlBtn.hBallAutoMoveTimeId = setTimeout(function(){
					rocker.animation.hBallAutoMoveToOrign();
				},300);
				rocker.pressControlBtn.degRightBtnTime.clear();
			},
			pressSpeedUpBtn: function() {
				clearTimeout(rocker.pressControlBtn.vBallAutoMoveTimeId);
				
				rocker.animation.clearVBallAutoMoveToOrign();
				rocker.pressControlBtn.speedUpBtnTime.init({
					currentCount: 0,
					repeatCount: 1000000,
					intervalTime: 20,
					delayTime: 300,
					timingFun: function() {
						rocker.vBallPos.x = rocker.vBallPos.x + 6;
						rocker.$setMoveBall();
					},
				}).start();
				rocker.vBallPos.x = rocker.vBallPos.x + 20;
				rocker.$setMoveBall();
			},
			pressEndSpeedUpBtn: function() {
				rocker.pressControlBtn.vBallAutoMoveTimeId = setTimeout(function(){
					rocker.animation.vBallAutoMoveToOrign();
				},300);
				rocker.pressControlBtn.speedUpBtnTime.clear();
			},
			pressSpeedDownBtn: function() {
				clearTimeout(rocker.pressControlBtn.vBallAutoMoveTimeId);
				
				rocker.animation.clearVBallAutoMoveToOrign();
				rocker.pressControlBtn.speedDownBtnTime.init({
					currentCount: 0,
					repeatCount: 1000000,
					intervalTime: 20,
					delayTime: 300,
					timingFun: function() {
						rocker.vBallPos.x = rocker.vBallPos.x - 6;
						rocker.$setMoveBall();
					},
				}).start();
				rocker.vBallPos.x = rocker.vBallPos.x - 20;
				rocker.$setMoveBall();
			},
			pressEndSpeedDownBtn: function() {
				rocker.pressControlBtn.vBallAutoMoveTimeId = setTimeout(function(){
					rocker.animation.vBallAutoMoveToOrign();
				},300);
				rocker.pressControlBtn.speedDownBtnTime.clear();
			},
		}
	};

	let data = {
		autoMoveToOrign: {
			isHBallAutoMove: true,
			isVBallAutoMove: true,
		},
		deg: 0,
		speed: 0,
		msg: ""
	}

	function manualContol() {
		let speed = Math.round(Math.sqrt(Math.abs(rocker.vBallPos.x) / rocker.maxY) * 254);
		let servoDeg = Math.round(rocker.hBallPos.y / rocker.maxX * 60);
		if(rocker.vBallPos.x < 0) {
			speed = -speed;
		}
		data.deg = servoDeg;
		data.speed = speed;
		if(bluetoothStore.connectedDevice) {
			bluetoothStore.sendDataFunc("[mc:" + speed + "&" + servoDeg + "]");
		}
		data.msg = "[mc:" + speed + "&" + servoDeg + "]";
	}

	export default {
		name: 'Rocker',
		data() {
			return data
		},
		props: {
			"autoMove": {
				type: Boolean,
				default: true
			},
			"ballPos": {
				type: Object,
				default: function() {
					return {
						x: 0,
						y: 0,
						maxX: 0,
						maxY: 0
					}
				}
			},
			"beforeClose": {
				type: Function
			}
		},
		created: function() {
			rocker.animation.isHBallAutoMove = this.autoMoveToOrign.isHBallAutoMove;
			rocker.animation.isVBallAutoMove = this.autoMoveToOrign.isVBallAutoMove;
			if(!bluetoothStore.connectedDevice) {
				data.msg = "当前未连接到蓝牙设备";
			}
			this.horizontalScreen();
		},
		computed: {

		},
		watch: {
			"autoMoveToOrign.isHBallAutoMove": function() {
				rocker.animation.isHBallAutoMove = this.autoMoveToOrign.isHBallAutoMove;
			},
			"autoMoveToOrign.isVBallAutoMove": function() {
				rocker.animation.isVBallAutoMove = this.autoMoveToOrign.isVBallAutoMove;
			}
		},
		mounted: function() {
			let that = this;

			let rockerWrap = document.getElementById("rocker-wrap");
			rockerWrap.style.width = window.innerHeight + "px";
			rockerWrap.style.height = window.innerWidth + "px";
			setTimeout(function() {
				rocker.init();
				rocker.maxY = Math.round(rocker.$el.offsetHeight * 0.45);
				rocker.maxX = Math.round(240 * 0.45);
				that.ballPos.maxX = rocker.maxX;
				that.ballPos.maxY = rocker.maxY;
			}, 2000);

		},
		methods: {
			closeRocker: function() {
				this.beforeClose && this.beforeClose();
			},
			onChangeHBallAutoMoveToggle: function(e) {
				let checked = e.target.checked;
				this.autoMoveToOrign.isHBallAutoMove = checked;
			},
			onChangeVBallAutoMoveToggle: function(e) {
				let checked = e.target.checked;
				this.autoMoveToOrign.isVBallAutoMove = checked;
			},
			horizontalScreen: function() {
				document.body.style.top = "0";
				document.body.style.left = "100%";
				document.body.style.transformOrigin = "0 0";
				document.body.style.transform = "rotateZ(90deg)";
				document.body.style.width = window.innerHeight + "px";
				document.body.style.height = window.innerWidth + "px";
			},
			clearHorizontalScreen: function() {
				document.body.style.left = "";
				document.body.style.top = "";
				document.body.style.transformOrigin = "";
				document.body.style.transform = "";
				document.body.style.width = "";
				document.body.style.height = "";
			}

		},
		destroyed: function() {
			rocker.destroyed();
			this.clearHorizontalScreen();
		}
	}
</script>

<style scoped>
	.back-btn { position: absolute; width: 40px; height: 40px; border-radius: 50%; top: 10px; left: 10px;}
	.control-panel { position: absolute; z-index: 2; top: 10px; left: 80px; padding: 5px;}
	.control-panel-item { height: 35px; display: flex; align-items: center;}
	.control-panel-item .title { width: 120px;}
	.msg-panel { position: absolute; z-index: 2; top: 10px; left: 260px; padding: 5px;}
	.msg-panel-item { height: 35px; display: flex; align-items: center;}
	.msg-panel-item .title { width: 80px;}
	.msg-panel-item .msg {display: inline-block;font-weight: bold; font-size: 16px;width: 50px;}
	#rocker-wrap { position: fixed; top: 0; background: white; z-index: 2;}
	#rocker {touch-action: none;position: relative;width: 100%;height: 100%; display: flex; justify-content: space-between;align-items: flex-end; border: 2px solid gray;box-sizing: border-box;}
	#horizontal {position: relative;width: 240px;height: 120px; display: flex; justify-content: center; align-items: center; border: 1px solid groove;}
	#horizontal .control-btn-wrap { position: absolute; top: -20px; left: 0; width: 100%;}
	#horizontal .control-btn-wrap .msg { position: absolute; width: 50px; height: 40px; line-height: 40px; text-align: center; font-weight: bold; font-size: 18px; left: 50%; transform: translateX(-50%);}
	#horizontal .control-btn-wrap button { position: absolute; width: 50px; height: 40px;}
	#horizontal .control-btn-wrap i { vertical-align: middle;}
	#horizontal .track {width: 90%; height: 10px; background: red;}
	#horizontal .rocker-ball { position: absolute; width: 50px; height: 50px; border-radius: 50%; background: gray;}
	#vertical { width: 100px;height: 100%; display: flex; justify-content: center; align-items: center; border: 1px solid groove; position: relative;}
	#vertical .control-btn-wrap { position: absolute; top: 0px; left: -35px; height: 100%;}
	#vertical .control-btn-wrap .msg { position: absolute; width: 50px; height: 40px; line-height: 40px; text-align: center; font-weight: bold; font-size: 16px; top: 50%; transform: translateY(-50%);}
	#vertical .control-btn-wrap button { position: absolute; width: 50px; height: 40px; transform-origin: top;}
	#vertical .control-btn-wrap i { vertical-align: middle;}
	#vertical .track { width: 10px;height: 90%; background: red;}
	#vertical .rocker-ball { position: absolute; width: 50px; height: 50px;border-radius: 50%; background: gray;}
</style>