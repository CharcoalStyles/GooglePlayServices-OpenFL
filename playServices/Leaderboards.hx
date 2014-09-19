package playServices;

/**
 * ...
 * @author 
 */
class Leaderboards
{

	#if android
	private static var _submitScore_func : Dynamic;
	private static var _showLeaderboard_func : Dynamic;
	private static var _showAllLeaderboards_func : Dynamic;
	
	public static function submitScore(boardId:String, score:Int):Void
	{
		if (_submitScore_func == null)
			_submitScore_func = openfl.utils.JNI.createStaticMethod("org.haxe.lime.GameActivity", "submitScore", "(Ljava/lang/String;I)V", true);
			
		var args = new Array<Dynamic>();
		args.push(boardId);
		args.push(score);
		
		_submitScore_func(args);
	}
	
	public static function showLeaderboard(boardId:String):Void
	{			
		if (_showLeaderboard_func == null)
			_showLeaderboard_func = openfl.utils.JNI.createStaticMethod("org.haxe.lime.GameActivity", "showLeaderboard", "(Ljava/lang/String;)V", true);
			
		var args = new Array<Dynamic>();
		args.push(boardId);
		
		_showLeaderboard_func(args);
	}
	
	public static function showAllLeaderboards():Void
	{
		if (_showAllLeaderboards_func == null)
			_showAllLeaderboards_func = openfl.utils.JNI.createStaticMethod("org.haxe.lime.GameActivity", "showAllLeaderboards", "()V", true);
			
		var args = new Array<Dynamic>();
		
		_showAllLeaderboards_func(args);
	}
	
	#end
	
}