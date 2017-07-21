# rn-barcode-scanner

A Barcode scanner for React Native.

![](http://i1.buimg.com/601556/3cd5d77587fa38bc.png)


## Installation
1. install from npm

    `npm i --save rn-barcode-scanner `
 
2. link it to you android project

    `react-native link`
        

## Usage

```
import {BarFormat, openBarcodeScanner} from 'rn-barcode-scanner';

//....

class TextComp extends Component{

    doIt(){
        openBarcodeScanner([BarFormat.QR_CODE]).then(
            (result)=>{
                //decoded string from barcode                
            },
            (err)=>{
            
            }
        );
    }
}

//....


```

## Api

- open the barcode scanner
```
/*
* 
* @param formats  supported barcode formats to detect, default is [BarFormat.QR_CODE]
* @param title    decode window's title, default is null
* @param tip      some tip for user to put barcode to the scanning window, default is null
*/
openBarcodeScanner(formats?:Array<string>, title?:string, tip?: string ):Promise<string> 

```



- supported barcode formats

```jsx harmony
BarFormat.QR_CODE
BarFormat.AZTEC
BarFormat.CODABAR
BarFormat.CODE_39
BarFormat.CODE_93
BarFormat.CODE_128
BarFormat.DATA_MATRIX
BarFormat.EAN_8
BarFormat.EAN_13
BarFormat.ITF
BarFormat.MAXICODE
BarFormat.PDF_417
BarFormat.QR_CODE
BarFormat.RSS_14
BarFormat.RSS_EXPANDED
BarFormat.UPC_A
BarFormat.UPC_E
BarFormat.UPC_EAN_EXTENSION

```





