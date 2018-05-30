<template>
	<f7-page>
		<f7-navbar>
			<f7-nav-left>
				<f7-link icon-if-ios="f7:menu" icon-if-md="material:menu" panel-open="left"></f7-link>
			</f7-nav-left>
			<f7-nav-title>智能小车</f7-nav-title>
		</f7-navbar>

		<f7-list>
			<f7-list-item link="/car/bluetooth/" panel-close title="蓝牙小车"></f7-list-item>
			<f7-list-item link="/car/wifi/" panel-close title="wifi小车"></f7-list-item>
		</f7-list>
		
		<f7-block strong>
	      <p>蓝牙小车上位机, 基于 cordova + framework7 + vue.js开发</p>
	      <p>蓝牙小车下位机使用arduino来制作</p>
	      <p>源代码在: https://github.com/chengxg/small-car</p>
	    </f7-block>

	</f7-page>
</template>
<script>
	export default {}
</script>