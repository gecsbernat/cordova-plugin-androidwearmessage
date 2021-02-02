var exec = require('cordova/exec');
module.exports = {
	initialize: function (success, error) {
		exec(success, error, "AndroidWearMessage", "initialize", []);
	},
	sendMessage: function (message, success, error) {
		exec(success, error, "AndroidWearMessage", "sendMessage", [message]);
	},
	listenMessage: function (success, error) {
		exec(success, error, "AndroidWearMessage", "listenMessage", []);
	}
};