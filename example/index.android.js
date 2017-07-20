/**
 * Sample React Native App
 * https://github.com/facebook/react-native
 * @flow
 */

import React, {Component} from 'react';
import {
	AppRegistry,
	StyleSheet,
	View,
	Button
} from 'react-native';
import {BarFormat, openBarcodeScanner} from 'rn-barcode-scanner';

export default class example extends Component {
	render() {
		return (
			<View style={styles.container}>
				<Button  title="open scanner" onPress={() => {
					this.openScanner()
				}}/>
			</View>
		);
	}
	
	openScanner() {
		openBarcodeScanner([BarFormat.QR_CODE, BarFormat.AZTEC]).then(
			(result) => {
				console.log("result is " + result);
			},
			(err) => {
				console.log("failed");
			}
		)
	}
}

const styles = StyleSheet.create({
	container: {
		flex: 1,
		justifyContent: 'center',
		alignItems: 'center',
		backgroundColor: '#F5FCFF',
	},
	welcome: {
		fontSize: 20,
		textAlign: 'center',
		margin: 10,
	},
	instructions: {
		textAlign: 'center',
		color: '#333333',
		marginBottom: 5,
	},
});

AppRegistry.registerComponent('example', () => example);
