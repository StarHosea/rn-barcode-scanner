import {NativeModules} from 'react-native';

const RNBarCodeScanner = NativeModules.BarCodeScanner;


export const BarFormat: {
	QR_CODE: string,
	AZTEC: string,
	CODABAR: string,
	CODE_39: string,
	CODE_93: string,
	CODE_128: string,
	DATA_MATRIX: string,
	EAN_8: string,
	EAN_13: string,
	ITF: string,
	MAXICODE: string,
	PDF_417: string,
	QR_CODE: string,
	RSS_14: string,
	RSS_EXPANDED: string,
	UPC_A: string,
	UPC_E: string,
	UPC_EAN_EXTENSION: string,
	
} = {
	QR_CODE: RNBarCodeScanner.QR_CODE,
	AZTEC: RNBarCodeScanner.AZTEC,
	CODABAR: RNBarCodeScanner.CODABAR,
	CODE_39: RNBarCodeScanner.CODE_39,
	CODE_93: RNBarCodeScanner.CODE_93,
	CODE_128: RNBarCodeScanner.CODE_93,
	DATA_MATRIX: RNBarCodeScanner.DATA_MATRIX,
	EAN_8: RNBarCodeScanner.EAN_8,
	EAN_13: RNBarCodeScanner.EAN_13,
	ITF: RNBarCodeScanner.ITF,
	MAXICODE: RNBarCodeScanner.MAXICODE,
	PDF_417: RNBarCodeScanner.PDF_417,
	RSS_14: RNBarCodeScanner.RSS_14,
	RSS_EXPANDED: RNBarCodeScanner.RSS_EXPANDED,
	UPC_A: RNBarCodeScanner.UPC_A,
	UPC_E: RNBarCodeScanner.UPC_E,
	UPC_EAN_EXTENSION: RNBarCodeScanner.UPC_EAN_EXTENSION
};

export function openBarcodeScanner(formats?: Array<string>, title?: string, tip?: string): Promise<string> {
	return RNBarCodeScanner.openBarCodeScanner(formats, title, tip);
}