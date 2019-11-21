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
    private static final int TAKE_PICTURE_REQUEST_CODE = 1;
    private static final int OPEN_GALLERY_REQUEST_CODE = 2;

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


    ////////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////
    //                                  Camera                                    //
    ////////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////

    private File tempImage;

    private void takePicture(){
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        final String file = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES) + "/MlKitPhoto.jpg";
        File newFile = new File(file);
        try{
            newFile.createNewFile();
        }catch(IOException e){

        }
        tempImage = newFile;
        Activity act = this.cordova.getActivity();
        String packagename = act.getComponentName().getPackageName();
        Uri imageUri = FileProvider.getUriForFile(
                act.getApplicationContext(),
                packagename,
                tempImage);
        takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT,imageUri);

        if (this.cordova != null) {
            PackageManager mPm = act.getPackageManager();
            if(takePictureIntent.resolveActivity(mPm) != null)
            {
                this.cordova.startActivityForResult((CordovaPlugin) this, takePictureIntent, TAKE_PICTURE_REQUEST_CODE);
            }
            else
            {
                Log.d(TAG, "Error: You don't have a default camera.  Your device may not be CTS complaint.");
            }
        }
    }

    private void openPhotoLibrary(){
        Intent PictureIntent = new Intent(Intent.ACTION_PICK,MediaStore.Images.Media.EXTERNAL_CONTENT_URI);

        if (this.cordova != null) {
            // Let's check to make sure the camera is actually installed. (Legacy Nexus 7 code)
            PackageManager mPm = this.cordova.getActivity().getPackageManager();
            if (PictureIntent.resolveActivity(mPm) != null) {
                this.cordova.startActivityForResult((CordovaPlugin) this, PictureIntent, OPEN_GALLERY_REQUEST_CODE);
            } else {
                Log.d(TAG, "Error: You don't have a default camera.  Your device may not be CTS complaint.");
            }
        }
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode,resultCode,data);
        if (resultCode == RESULT_OK){
            if (requestCode == TAKE_PICTURE_REQUEST_CODE) {
                //Bundle extras = data.getExtras();
                //Bitmap imageBitmap = (Bitmap) extras.get("data");
                try{
                    Activity act = this.cordova.getActivity();
                    String packagename = act.getComponentName().getPackageName();
                    Uri imageUri = FileProvider.getUriForFile(
                            act,
                            packagename, //(use your app signature + ".provider" )
                            tempImage);
                    final Bitmap imageBitmap = MediaStore.Images.Media.getBitmap(cordova.getActivity().getContentResolver(),imageUri);

                    //call text identification, label recognition, face detection

                    return;
                }catch (IOException e){
                    _callbackContext.error("Could not retrieve image!");
                    return;
                }
            }else if (requestCode == OPEN_GALLERY_REQUEST_CODE){
                Uri selectedImage = data.getData();
                try{
                    final Bitmap imageBitmap = MediaStore.Images.Media.getBitmap(cordova.getActivity().getContentResolver(),selectedImage);
                    //call text identification, label recognition, face detection

                    return;
                }catch (IOException e){
                    _callbackContext.error("Could not retrieve image!");
                    return;
                }
            }
        }
        _callbackContext.error("No Image Selected!");


    }

    protected void requestPermission(String permission, int requestId) throws Exception {
        Boolean granted = hasPermission(permission);
        if (granted) {
            if (permission.equals(Manifest.permission.CAMERA)) {
                takePicture();
            } else if (permission.equals(Manifest.permission.READ_EXTERNAL_STORAGE)) {
                openPhotoLibrary();
            }
        } else {
            try {
                java.lang.reflect.Method method = cordova.getClass().getMethod("requestPermission", org.apache.cordova.CordovaPlugin.class, int.class, java.lang.String.class);
                method.invoke(cordova, this, requestId, permission);
            } catch (NoSuchMethodException e) {
                throw new Exception("requestPermissions() method not found in CordovaInterface implementation of Cordova v" + CordovaWebView.CORDOVA_VERSION);
            }
        }
    }

    protected boolean hasPermission(String permission) throws Exception {
        boolean hasPermission = true;
        Method method;
        try {
            method = cordova.getClass().getMethod("hasPermission", permission.getClass());
            Boolean bool = (Boolean) method.invoke(cordova, permission);
            hasPermission = bool.booleanValue();
        } catch (NoSuchMethodException e) {
            Log.w(TAG, "Cordova v" + CordovaWebView.CORDOVA_VERSION + " does not support runtime permissions so defaulting to GRANTED for " + permission);
        }
        return hasPermission;
    }

    protected boolean shouldShowRequestPermissionRationale(Activity activity, String permission) throws Exception {
        boolean shouldShow;
        try {
            java.lang.reflect.Method method = ActivityCompat.class.getMethod("shouldShowRequestPermissionRationale", Activity.class, java.lang.String.class);
            Boolean bool = (Boolean) method.invoke(null, activity, permission);
            shouldShow = bool.booleanValue();
        } catch (NoSuchMethodException e) {
            throw new Exception("shouldShowRequestPermissionRationale() method not found in ActivityCompat class. Check you have Android Support Library v23+ installed");
        }
        return shouldShow;
    }

    public void onRequestPermissionResult(int requestCode, String[] permissions, int[] grantResults) throws JSONException {
        String sRequestId = String.valueOf(requestCode);
        Log.v(TAG, "Received result for permissions request id=" + sRequestId);
        try {
            for (int i = 0, len = permissions.length; i < len; i++) {
                String androidPermission = permissions[i];
                if (grantResults[i] == PackageManager.PERMISSION_DENIED) {
                    boolean showRationale = shouldShowRequestPermissionRationale(this.cordova.getActivity(), androidPermission);
                    if (!showRationale) {

                        if (androidPermission.equals(Manifest.permission.CAMERA)) {
                            _callbackContext.error("Camera Permission not allowed!");
                        } else if (androidPermission.equals(Manifest.permission.READ_EXTERNAL_STORAGE)) {
                            _callbackContext.error("Storage Permission not allowed!");
                        }
                    } else {
                        if (androidPermission.equals(Manifest.permission.CAMERA)) {
                            _callbackContext.error("Camera Permission not allowed!");
                        } else if (androidPermission.equals(Manifest.permission.READ_EXTERNAL_STORAGE)) {
                            _callbackContext.error("Storage Permission not allowed!");
                        }
                    }
                } else {
                    if (androidPermission.equals(Manifest.permission.CAMERA)) {
                        takePicture();
                    } else if (androidPermission.equals(Manifest.permission.READ_EXTERNAL_STORAGE)) {
                        openPhotoLibrary();
                    }
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Exception occurred onRequestPermissionsResult: ".concat(e.getMessage()));
            _callbackContext.error("Exception occurred onRequestPermissionsResult: ".concat(e.getMessage()));
        }
    }
}


