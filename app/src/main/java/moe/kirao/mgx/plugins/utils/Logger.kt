package moe.kirao.mgx.plugins.utils

import android.util.Log
import moe.kirao.mgx.plugins.Plugin

class Logger(val plugin: Plugin) {

	fun log(msg: String){
		Log.d("PyLogger [${plugin.name}]", msg)
	}

}