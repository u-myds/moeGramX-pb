package moe.kirao.mgx.plugins

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.Toast
import moe.kirao.mgx.plugins.Manager.loadedPlugins
import moe.kirao.mgx.ui.SettingsMoexController
import org.drinkless.tdlib.TdApi
import java.io.File

object PluginInstaller {

	fun log(message: String){
		Log.d("PluginInstaller", message)
	}

	fun install(context: Context, file: TdApi.File, original: TdApi.Document){
		val src = File(file.local.path)

		val dest = File(context.filesDir.resolve("plugins"), original.fileName.replace(".moeplugin", ".py"))
		if(dest.exists()){
			Manager.deletePlugin(original.fileName.replace(".moeplugin", ""))
		}
		src.copyTo(dest, overwrite = true)
		Manager.loadPlugin(dest)
		Handler(Looper.getMainLooper()).post {
			Toast.makeText(context, "File ${original.fileName.replace(".moeplugin", "")} installed", Toast.LENGTH_SHORT).show()
		}

		log(context.filesDir.resolve("plugins").listFiles()?.toString() ?: "Error reading dir")
		SettingsMoexController.loadedPlugins = loadedPlugins
	}
}