package playServices;

/**
 * ...
 * @author 
 */
class Achievements
{
	#if android
	private static var _unlockAchievemnt_func : Dynamic;
	private static var _incrementAchievement_func : Dynamic;
	private static var _showAchievements_func : Dynamic;
	
	public static function unlockAchievemnt(achievementId:String):Void
	{
		if (_unlockAchievemnt_func == null)
			_unlockAchievemnt_func = openfl.utils.JNI.createStaticMethod("org.haxe.lime.GameActivity", "unlockAchievemnt", "(Ljava/lang/String;)V", true);
			
		var args = new Array<Dynamic>();
		args.push(achievementId);
		
		_unlockAchievemnt_func(args);
	}
	
	public static function incrementAchievemnt(achievementId:String, amout:Int):Void
	{			
		if (_incrementAchievement_func == null)
			_incrementAchievement_func = openfl.utils.JNI.createStaticMethod("org.haxe.lime.GameActivity", "incrementAchievemnt", "(Ljava/lang/String;I)V", true);
			
		var args = new Array<Dynamic>();
		args.push(achievementId);
		args.push(amout);
		
		_incrementAchievement_func(args);
	}
	
	public static function showAchievements():Void
	{
		if (_showAchievements_func == null)
			_showAchievements_func = openfl.utils.JNI.createStaticMethod("org.haxe.lime.GameActivity", "showAchievements", "()V", true);
			
		var args = new Array<Dynamic>();
		
		_showAchievements_func(args);
	}
	
	#end
}