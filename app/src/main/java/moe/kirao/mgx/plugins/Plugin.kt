package moe.kirao.mgx.plugins

import com.chaquo.python.PyObject
import de.robv.android.xposed.XC_MethodHook

data class Plugin(
	var id: Int? = null,
	val name: String,
	var obj: PyObject? = null,
	var enabled: Boolean,
	var author: String? = null,
	var description: String? = null,
)
