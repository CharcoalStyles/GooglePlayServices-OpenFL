package android;

/**
 * ...
 * @author 
 */
class Log
{
	#if android
	private static var _log_func : Dynamic;
	
	/**
	 * Puts a log line into the Android device's LogCat.
	 * Unless set up, ADB does not capture logs of type 3 or below.
	 * Type correspond to the constants in android.util.Log (http://developer.android.com/reference/android/util/Log.html)
	 * 
	 * @param 	tag		A tag to identify your log
	 * @param 	message The message for your log
	 * @param 	type	The type of message; from 2 to 7.
	 */
	public static function log(tag:String, message:String, ?type:Int=3):Void
	{
		if (_log_func == null)
			_log_func = openfl.utils.JNI.createStaticMethod("org.haxe.lime.GameActivity", "log", "(ILjava/lang/String;Ljava/lang/String;)V", true);
			
		var args = new Array<Dynamic>();
		args.push(type);
		args.push(tag);
		args.push(message);
		
		_log_func(args);
	}
	
	#end
}