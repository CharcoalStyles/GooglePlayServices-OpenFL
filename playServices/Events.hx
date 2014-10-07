package playServices;

/**
 * @author SeiferTim - tims-world.com
 */
class Events
{

	#if android
	private static var _incrementEvent_func:Dynamic;
	
	public static function incrementEvent(EventID:String, Amount:Int):Void
	{
		if (_incrementEvent_func == null)
		{
			_incrementEvent_func = openfl.utils.JNI.createStaticMethod("org.haxe.lime.GameActivity", "incrementEvent", "(Ljava/lang/String;I)V", true);
		}
		
		var args = new Array<Dynamic>();
		args.push(EventID);
		args.push(Amount);
		_incrementEvent_func(args);
	}
	#end
	
}