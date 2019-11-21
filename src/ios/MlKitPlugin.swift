/********* MlFirebasePlugin.m Cordova Plugin Implementation *******/

import Foundation

import WebKit

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
    
    //***
    //***
    //***** Utilities
    //***
    //***
    
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
