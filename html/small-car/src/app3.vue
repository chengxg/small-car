<template>
	<v-app id="inspire">
		<v-navigation-drawer v-model="drawer" fixed app>
			<v-list dense>
				<v-list-tile @click="">
					<v-list-tile-action>
						<v-icon>home</v-icon>
					</v-list-tile-action>
					<v-list-tile-content>
						<v-list-tile-title>Home</v-list-tile-title>
					</v-list-tile-content>
				</v-list-tile>
				<v-list-tile @click="">
					<v-list-tile-action>
						<v-icon>contact_mail</v-icon>
					</v-list-tile-action>
					<v-list-tile-content>
						<v-list-tile-title>Contact</v-list-tile-title>
					</v-list-tile-content>
				</v-list-tile>
			</v-list>
		</v-navigation-drawer>
		<v-toolbar color="indigo" dark fixed app>
			<v-toolbar-side-icon @click.stop="drawer = !drawer"></v-toolbar-side-icon>
			<v-toolbar-title>Application</v-toolbar-title>
		</v-toolbar>
		<v-content>
			<v-container fluid fill-height>
				<v-layout justify-center align-center>
					<v-flex text-xs-center>
						<v-tooltip left>
							<v-btn slot="activator" :href="source" icon large target="_blank">
								<v-icon large>code</v-icon>
							</v-btn>
							<span>Source</span>
						</v-tooltip>
						<v-tooltip right>
							<v-btn slot="activator" icon large href="https://codepen.io/johnjleider/pen/rJdVMq" target="_blank">
								<v-icon large>mdi-codepen</v-icon>
							</v-btn>
							<span>Codepen</span>
						</v-tooltip>
					</v-flex>
				</v-layout>

				<v-expansion-panel>
					<v-expansion-panel-content v-for="(item,i) in 5" :key="i">
						<div slot="header">Item</div>
						<v-card>
							<v-card-text>Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris nisi ut aliquip ex ea commodo consequat.</v-card-text>
						</v-card>
					</v-expansion-panel-content>
				</v-expansion-panel>
			</v-container>
		</v-content>
	</v-app>
</template>

<script>
	export default {
		data: () => ({
			drawer: null
		}),
		props: {
			source: String
		}
	}
</script>