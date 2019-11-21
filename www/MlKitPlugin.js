var exec = require('cordova/exec');

exports.getText = function (success, error,takePicture,cloud,lang) {

    var args = [takePicture,cloud,lang];

    exec(success, error, 'MlKitPlugin', 'getText', args);
};
