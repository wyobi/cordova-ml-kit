var exec = require('cordova/exec');

exports.getText = function (success, error,takePicture,cloud,lang) {

    var args = [takePicture,cloud,lang];

    exec(success, error, 'MlKitPlugin', 'getText', args);
};
exports.getLabel = function (success, error,takePicture,cloud) {

    var args = [takePicture,cloud];

    exec(success, error, 'MlKitPlugin', 'getLabel', args);
};
exports.getFace = function (success, error,takePicture,options) {

    var args = [takePicture,options];

    exec(success, error, 'MlKitPlugin', 'getFace', args);
};
