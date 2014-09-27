package playServices;
import androidUtil.Logging;

/**
 * ...
 * @author 
 */
class IAP
{
	public static var isIapSetup:Bool = false;
	public static var retrievedPurchases:Bool = false;
	public static var firstRetrievedPurchases:Bool = false;

	private static var _setupIAP_func : Dynamic;
	private static var _getPurchases_func : Dynamic;
	private static var _lauchPurchaseFlow_func : Dynamic;
	private static var purchaseHelper:Purchases;
	
	public static var purchases:Array<PurchaseInfo>;
	
	public static function setupIap(key:String):Void
	{
		#if android
		// call API
		if (_setupIAP_func == null) {
			_setupIAP_func = openfl.utils.JNI.createStaticMethod("org.haxe.lime.GameActivity", "setupIap",
				"(Ljava/lang/String;)Z", true);
		}

		var args = new Array<Dynamic>();
		args.push(key);
		var setup:Bool = _setupIAP_func(args);
		
		purchases = new Array<PurchaseInfo>();
		purchaseHelper = new Purchases();
		isIapSetup = true;
			
		#end
	}
	
	public static function lauchPurchaseFlow(productID:String, requestCode:Int, payload:String):Void
	{
		#if android
		if (isIapSetup)
		{
			if (_lauchPurchaseFlow_func == null) {
				_lauchPurchaseFlow_func = openfl.utils.JNI.createStaticMethod("org.haxe.lime.GameActivity", "lauchPurchaseFlow",
					"(Ljava/lang/String;ILjava/lang/String;)V", true);
			}

			var args = new Array<Dynamic>();
			args.push(productID);
			args.push(requestCode);
			args.push(payload);
			_lauchPurchaseFlow_func(args);
		}
		#end
	}
	
	public static function retrievePurchases():Void
	{
		#if android
		if (isIapSetup && (retrievedPurchases || !firstRetrievedPurchases))
		{
			firstRetrievedPurchases = true;
			retrievedPurchases = false;
			if (_getPurchases_func == null) {
				_getPurchases_func = openfl.utils.JNI.createStaticMethod("org.haxe.lime.GameActivity", "retrievePurchases",
					"()V", true);
			}

			var args = new Array<Dynamic>();
			_getPurchases_func(args);
		}
		#end
	}
	
	public static function hasProduct(productID:String)
	{
		#if android
		var hp:Bool = false;
		if (retrievedPurchases)
		{
			for (pi in purchases)
			{
				if (pi.productID == productID)
					hp = true;
			}
		}
		return hp;
		#end
	}
	
	/*Test function */
	private static var _testIapCallback_func : Dynamic;
	public static function testReturnIapObject():Void
	{
		#if android
		if (isIapSetup)
		{
			if (_testIapCallback_func == null) {
				_testIapCallback_func = openfl.utils.JNI.createStaticMethod("org.haxe.lime.GameActivity", "testReturnIapObject",
					"()V", true);
			}

			var args = new Array<Dynamic>();
			_testIapCallback_func(args);
		}
		#end
	}
	
}

typedef PurchaseInfo = {
	var productID : String;
	var requestCode : Int;
	var payload :String;
}

class Purchases
{	
	public function new()
	{
		#if android
		var fn = openfl.utils.JNI.createStaticMethod("org.haxe.lime.GameActivity", "setupIapCallback", "(Lorg/haxe/lime/HaxeObject;)V", true);
		fn([this]);
		#end
	}

	public function onPurchase(pid:String, reqCode:Int, pl:String)
	{
		if (pid != "EOL")
		{
			var skip:Bool = false;
			
			for (iap in IAP.purchases)
			{
				if (!skip && iap.productID == pid)
					skip = true;
			}
			
			if (!skip)
			{
				var pi:PurchaseInfo = {
					productID: pid,
					requestCode: reqCode,
					payload: pl
				};
				IAP.purchases.push(pi);
			}
		}
		else
		{
			IAP.retrievedPurchases = true;
		}
	} 
}