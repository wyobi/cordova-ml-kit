/********* MlFirebasePlugin.m Cordova Plugin Implementation *******/

import Foundation

import WebKit

import AVFoundation

import MobileCoreServices

var _command: CDVInvokedUrlCommand!

@objc(MlKitPlugin) class MlKitPlugin : CDVPlugin{
    
    static let LOG_TAG = "ML-Kit-Plugin";
    
    var action: Actions?
    
    var options:[String:Any]?

    enum Actions {
        case INVALID
        case COOLMETHOD
        
        static let allValues = [INVALID,COOLMETHOD]
        
    }
    
    @objc(coolMethod:)
    func coolMethod(command: CDVInvokedUrlCommand){
        options = [String:Any]()
        
        action = Actions.COOLMETHOD
        _command=command

        options = (command.argument(at: 0, withDefault:["no":"no"]) as! [String:Any])
    }
    
    
    ////////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////
    //                                  Camera                                    //
    ////////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////

    func useCamera() {
        
        if UIImagePickerController.isSourceTypeAvailable(
            UIImagePickerControllerSourceType.camera) {
            DispatchQueue.main.async {
                let imagePicker = UIImagePickerController()
                
                imagePicker.delegate = self
                imagePicker.sourceType =
                    UIImagePickerControllerSourceType.camera
                imagePicker.mediaTypes = [kUTTypeImage as String]
                imagePicker.allowsEditing = false
                
                self.viewController.present(imagePicker, animated: true,
                                            completion: {
                                                imagePicker.delegate = self
                })
            }
        }
    }
    
    func useCameraRoll() {
        
        if UIImagePickerController.isSourceTypeAvailable(
            UIImagePickerControllerSourceType.savedPhotosAlbum) {
            
            DispatchQueue.main.async {
                let imagePicker = UIImagePickerController()
                
                imagePicker.delegate = self
                imagePicker.sourceType =
                    UIImagePickerControllerSourceType.photoLibrary
                imagePicker.mediaTypes = [kUTTypeImage as String]
                imagePicker.allowsEditing = false
                
                self.viewController.present(imagePicker, animated: true,
                                            completion: {
                                                imagePicker.delegate = self
                })
            }
        }
    }
    
    func imagePickerController(_ picker: UIImagePickerController, didFinishPickingMediaWithInfo info: [String : Any]) {
        let mediaType = info[UIImagePickerControllerMediaType] as! NSString
        
        viewController.dismiss(animated: true, completion: nil)
        
        if mediaType.isEqual(to: kUTTypeImage as String) {
            let image = info[UIImagePickerControllerOriginalImage]
                as! UIImage
            
            let visionImage = getVisionImage(in: image)
            switch action {
            case .GETTEXT:
                runTextRecognition(for: visionImage, onCloud: options!["Cloud"] as! Bool , in: options!["language"] as! String, call: _command)
            case .GETLABLE:
                runLabelIdentifier(for: visionImage, onCloud: options!["Cloud"] as! Bool, call: _command)
            case .GETFACE:
                let visionOptions = VisionFaceDetectorOptions()
                visionOptions.performanceMode = VisionFaceDetectorPerformanceMode.init(rawValue: options?["Performance"] as? UInt ?? 2) ?? VisionFaceDetectorPerformanceMode.accurate
                visionOptions.landmarkMode = VisionFaceDetectorLandmarkMode.init(rawValue: options?["Landmark"] as? UInt ?? 2) ?? VisionFaceDetectorLandmarkMode.all
                visionOptions.classificationMode = VisionFaceDetectorClassificationMode.init(rawValue: options?["Classification"] as? UInt ?? 2) ?? VisionFaceDetectorClassificationMode.all
                visionOptions.contourMode = VisionFaceDetectorContourMode.init(rawValue: options?["Contours"] as? UInt ?? 1) ?? VisionFaceDetectorContourMode.none
                
                visionOptions.minFaceSize = CGFloat(options?["MinFaceSize"] as? Float ?? 0.1)
                
                options?["Tracking"] as? Bool ?? false
                
                
                runFaceDetection(for: visionImage,with: visionOptions, call:_command)
            default:
                sendPluginError(message: "Invalid Action detected after image selection!", call: _command)
            }
        }
    }
    
    func imagePickerController(_ picker: UIImagePickerController, didFinishPickingMediaWithInfo info: [UIImagePickerController.InfoKey : Any]) {
        
        let mediaType = info[UIImagePickerControllerMediaType as UIImagePickerController.InfoKey] as! NSString
        let image = info[UIImagePickerControllerOriginalImage as UIImagePickerController.InfoKey]
            as! UIImage
        
        var info2 = [String : Any]()
        info2[UIImagePickerControllerMediaType] = mediaType
        info2[UIImagePickerControllerOriginalImage] = image
        imagePickerController(picker, didFinishPickingMediaWithInfo: info2)
    }
    
    ////////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////
    //                                 Utilities                                  //
    ////////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////
    
    func convertToJson(in json:[String:Any])->String{
        var finalString = "{"
        var notFirst = false
        for (jsonIdx,jsonItem) in json {
            if notFirst {
                finalString += ","
            }else{
                notFirst = true
            }
            finalString += "\"\(jsonIdx)\" : ";
            let jsonItemArray = jsonItem as? [[String:Any]]
            let jsonItemJson = jsonItem as? [String:Any]
            if jsonItemArray != nil {
                finalString += convertToJson(in: jsonItemArray!)
            }else if jsonItemJson != nil{
                finalString += convertToJson(in: jsonItemJson!)
            }else{
                let item = jsonItem as? String
                if item != nil {
                    finalString += "\"\(item!)\""
                }else{
                    let floatItem = jsonItem as? CGFloat
                    if floatItem != nil {
                        finalString += "\"\(floatItem!)\""
                    }else{
                        finalString += "\"\""
                    }
                    
                }
            }
        }
        finalString += "}"
        return finalString
    }
    
    func convertToJson(in json:[[String:Any]])->String{
        var finalString = "["
        var notFirst = false
        for item in json {
            if notFirst {
                finalString += ","
            }else{
                notFirst = true
            }
            finalString += convertToJson(in: item)
        }
        finalString += "]"
        return finalString
    }
    
    func sendPluginResult(message result:[String:Any],call command:CDVInvokedUrlCommand){
        let json = convertToJson(in: result)
        sendPluginResult(message: json, call: command)
    }
    
    func sendPluginResult(message result:[[String:Any]],call command:CDVInvokedUrlCommand){
        let json = convertToJson(in: result)
        sendPluginResult(message: json, call: command)
    }
    
    func sendPluginResult(message result:String,call command:CDVInvokedUrlCommand) {
        var pluginResult:CDVPluginResult;
        pluginResult = CDVPluginResult(status: CDVCommandStatus_OK, messageAs: result)
        commandDelegate.send(pluginResult, callbackId: command.callbackId)
    }
    
    func sendPluginError(message result:String,call command:CDVInvokedUrlCommand) {
        var pluginResult:CDVPluginResult;
        pluginResult = CDVPluginResult(status: CDVCommandStatus_ERROR, messageAs: result)
        logError(message: result)
        commandDelegate.send(pluginResult, callbackId: command.callbackId)
    }
    
    func handlePluginException(with exception: NSException,call command:CDVInvokedUrlCommand)
    {
        let pluginResult = CDVPluginResult(status:CDVCommandStatus_ERROR, messageAs:exception.reason);
        logError(message :"EXCEPTION: "+(exception.reason ?? "unknown!"));
        commandDelegate.send(pluginResult, callbackId:command.callbackId);
    }
    
    func executeGlobalJavascript(withScript jsString: String) {
        commandDelegate.evalJs(jsString);
    }
    
    func logDebug(message msg:String)
    {
        NSLog("%@: %@", MlFirebasePlugin.LOG_TAG, msg);
        let jsString = "console.log(\""+MlFirebasePlugin.LOG_TAG+": "+escapeDoubleQuotes(str: msg)+"\")";
        executeGlobalJavascript(withScript: jsString);
    }
    
    func logError(message msg:String)
    {
        NSLog("%@ ERROR: %@", MlFirebasePlugin.LOG_TAG, msg);
        let jsString = "console.error(\""+MlFirebasePlugin.LOG_TAG+": "+escapeDoubleQuotes(str: msg)+"\")";
        executeGlobalJavascript(withScript: jsString);
    }
    
    func escapeDoubleQuotes(str:String) ->String{
        return str.replacingOccurrences(of: "\"", with: "\\\"");
    }
    
}
