package moe.kirao.mgx.plugins.utils

import com.chaquo.python.PyObject
import com.chaquo.python.Python
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.XC_MethodReplacement
import de.robv.android.xposed.XposedBridge
import java.lang.reflect.Method


class HookUtilities {
	val py = Python.getInstance()
	var mtdHook: PyObject? = null
	var mtdReplace: PyObject? = null
	init {

		mtdHook = py.getModule("Hook").get("MethodHook")
		mtdReplace = py.getModule("Hook").get("MethodReplacement")
	}


	 fun hook_method(method: Method, hook: PyObject): XC_MethodHook.Unhook? {
		 if(py.builtins.callAttrThrows("isinstance", hook, mtdHook).toBoolean()){
			return XposedBridge.hookMethod(method, object : XC_MethodHook(){
				override fun beforeHookedMethod(param: MethodHookParam?) {

					hook.callAttrThrows("beforeHookedMethod", param)
				}

				override fun afterHookedMethod(param: MethodHookParam?) {
					hook.callAttrThrows("afterHookedMethod", param)
				}
			})
		} else if(py.builtins.callAttrThrows("isinstance", hook, mtdReplace).toBoolean()){
			 return XposedBridge.hookMethod(method, object : XC_MethodReplacement(){
				 override fun replaceHookedMethod(param: MethodHookParam?): Any? {
					 return hook.callAttrThrows("replaceHookedMethod", param)
				 }
			 })

		}
		 return null

	}

	fun find_class(clazz: String): Class<*>?{
		return try {
			Class.forName(clazz)
		} catch (e: ClassNotFoundException){
			null
		}
	}


}