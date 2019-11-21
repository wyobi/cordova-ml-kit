var exec = require('cordova/exec');

exports.getText = function (success, error,options) {

    exec(success, error, 'MlKitPlugin', 'getText', [options]);
};
exports.getLabel = function (success, error,options) {

    exec(success, error, 'MlKitPlugin', 'getLabel', [options]);
};
exports.getFace = function (success, error,options) {

    exec(success, error, 'MlKitPlugin', 'getFace', [options]);
};
