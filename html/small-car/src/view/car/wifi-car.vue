<template>
	<f7-page>
		<f7-navbar title="wifi小车控制" back-link="Back"></f7-navbar>

		<div class="list">
			<ul>
				<li>
					<a href="#" class="item-link item-content popup-open" data-popup="#wifi">
						<div class="item-media">
							
						</div>
						<div class="item-inner">
							<div class="item-title">
								建立连接
							</div>
						</div>
					</a>
				</li>
			</ul>
		</div>
		
		<!-- Popup -->
		<f7-popup id="wifi">
			<f7-view>
				<Wifi></Wifi>
			</f7-view>
		</f7-popup>

	</f7-page>
</template>

<script>
	import { wifiStore } from '@/store';
	import Wifi from '@/components/Wifi';


	export default {
		name: 'WifiCar',
		data: function() {
			return {
				
			}
		},
		created: function() {
			let that = this;
		},
		watch: {

		},
		methods: {
			
		},
		components: {
			Wifi,
		}
	}
</script>

<style scoped>

</style>