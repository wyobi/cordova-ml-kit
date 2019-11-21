package com.outsystems.mlkitplugin;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaInterface;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CordovaWebView;
import org.apache.cordova.PluginResult;
import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONException;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.content.Context;
import android.graphics.Rect;
import android.graphics.Point;

import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.FileProvider;
import android.util.Log;

import android.support.annotation.NonNull;

import com.google.android.gms.predictondevice.SmartReplyOptions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.ml.naturallanguage.FirebaseNaturalLanguage;
import com.google.firebase.ml.naturallanguage.languageid.FirebaseLanguageIdentification;
import com.google.firebase.ml.naturallanguage.languageid.FirebaseLanguageIdentificationOptions;
import com.google.firebase.ml.naturallanguage.languageid.IdentifiedLanguage;
import com.google.firebase.ml.naturallanguage.smartreply.FirebaseSmartReply;
import com.google.firebase.ml.naturallanguage.smartreply.FirebaseTextMessage;
import com.google.firebase.ml.naturallanguage.smartreply.SmartReplySuggestion;
import com.google.firebase.ml.naturallanguage.smartreply.SmartReplySuggestionResult;
import com.google.firebase.ml.vision.FirebaseVision;
import com.google.firebase.ml.vision.common.FirebaseVisionImage;
import com.google.firebase.ml.vision.common.FirebaseVisionPoint;
import com.google.firebase.ml.vision.face.FirebaseVisionFace;
import com.google.firebase.ml.vision.face.FirebaseVisionFaceContour;
import com.google.firebase.ml.vision.face.FirebaseVisionFaceDetector;
import com.google.firebase.ml.vision.face.FirebaseVisionFaceDetectorOptions;
import com.google.firebase.ml.vision.face.FirebaseVisionFaceLandmark;
import com.google.firebase.ml.vision.label.FirebaseVisionImageLabel;
import com.google.firebase.ml.vision.label.FirebaseVisionImageLabeler;
import com.google.firebase.ml.vision.text.FirebaseVisionCloudTextRecognizerOptions;
import com.google.firebase.ml.vision.text.FirebaseVisionText;
import com.google.firebase.ml.vision.text.FirebaseVisionTextRecognizer;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;

import java.util.Collection;
import java.util.Dictionary;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

import static android.app.Activity.RESULT_OK;

public class MlKitPlugin extends CordovaPlugin {
    private static final String TAG = "MlKitPlugin";

    private static Context context;

    private CallbackContext _callbackContext;

    private JSONObject options = new JSONObject();

    private Actions myAction;

    public void initialize(CordovaInterface cordova, CordovaWebView webView) {
        super.initialize(cordova, webView);
        context = cordova.getActivity().getApplicationContext();
    }

    public boolean execute(String action, JSONArray args, final CallbackContext callbackContext) throws JSONException {
        _callbackContext = callbackContext;
        String error = isValidCall(action,args);
        try{
            if(error.isEmpty()){
                myAction = Actions.fromString(action);
                switch(myAction){
                    case COOLMETHOD:
                        break;
                }
                return true;
            }else{
                callbackContext.sendPluginResult(
                        new PluginResult(PluginResult.Status.INVALID_ACTION,error));
            }
        }catch(Exception e){
            e.printStackTrace();
            callbackContext.error(e.getMessage());
        }
        return false;
    }


    private String isValidCall(String action, JSONArray args) {
        boolean isValid;
        Actions a = Actions.fromString(action);
        switch (a) {
            case COOLMETHOD:
                isValid = args.length() == 1;
                try {
                    args.getJSONObject(0);
                } catch (JSONException e) {
                    isValid = false;
                }
                return isValid ? "" : a.argsDesc;
            default:
                return Actions.INVALID.argsDesc;
        }
    }

    enum Actions {
        INVALID("", "Invalid action"),
        COOLMETHOD("coolMethod","Invalid arguments-> options: JSONObject");

        String name;
        String argsDesc;

        Actions(String name, String argsDesc) {
            this.name = name;
            this.argsDesc = argsDesc;
        }

        public static Actions fromString(String action) {
            for (Actions a : Actions.values()) {
                if (a.name.equalsIgnoreCase(action)) {
                    return a;
                }
            }
            return INVALID;
        }
    }
}


