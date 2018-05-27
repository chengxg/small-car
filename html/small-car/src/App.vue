<template>
	<!-- App -->
	<div id="app" class="color-theme-blue">

		<!-- Statusbar -->
		<div class="statusbar-overlay"></div>
		<!-- Left Panel -->
		<f7-panel left>
			<f7-navbar>
				<f7-nav-right>
					<f7-link icon-if-ios="f7:menu" icon-if-md="material:menu" panel-close></f7-link>
				</f7-nav-right>
				<f7-nav-title>智能小车</f7-nav-title>
			</f7-navbar>
			<f7-list>
				<f7-list-item link="/car/bluetooth/" panel-close title="蓝牙小车"></f7-list-item>
				<f7-list-item link="/about/" panel-close title="关于"></f7-list-item>
			</f7-list>
		</f7-panel>

		<!-- Main View -->
		<f7-view id="main-view" url="/" main></f7-view>
	</div>
</template>

<script>
	export default {}
</script>