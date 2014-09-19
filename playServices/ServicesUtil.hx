package playServices;

/**
 * ...
 * @author 
 */
class ServicesUtil
{
	#if android
	private static var _signIn_func : Dynamic;
	private static var _loggedIn_func : Dynamic;
	
	public static function signIn():Void
	{
		trace("playServices.ServicesUtil.signIn()");
		
		if (_signIn_func == null)
			_signIn_func = openfl.utils.JNI.createStaticMethod("org.haxe.lime.GameActivity", "userSignIn", "()V", true);
		
		_signIn_func(new Array<Dynamic>());
	}
	
	public static function isLoggedIn():Bool
	{
		if (_loggedIn_func == null)
			_loggedIn_func = openfl.utils.JNI.createStaticMethod("org.haxe.lime.GameActivity", "userLoggedIn", "()Z", true);
		
		return _loggedIn_func(new Array<Dynamic>());
	}
	#end
}