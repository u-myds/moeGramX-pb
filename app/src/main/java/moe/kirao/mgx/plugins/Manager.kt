package moe.kirao.mgx.plugins

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import androidx.core.content.edit
import com.chaquo.python.PyObject
import com.chaquo.python.Python
import com.chaquo.python.android.AndroidPlatform
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.XC_MethodReplacement
import de.robv.android.xposed.XposedBridge
import me.vkryl.core.lambda.Future
import me.vkryl.core.lambda.RunnableData
import moe.kirao.mgx.MoexConfig
import moe.kirao.mgx.plugins.utils.AndroidUtilities
import moe.kirao.mgx.plugins.utils.HookUtilities
import moe.kirao.mgx.plugins.utils.Logger
import moe.kirao.mgx.ui.SettingsMoexController
import org.drinkless.tdlib.Client
import org.drinkless.tdlib.TdApi
import org.thunderdog.challegram.MainActivity
import org.thunderdog.challegram.telegram.Tdlib
import org.thunderdog.challegram.ui.MessagesController
import java.io.File
import java.lang.reflect.Method
import java.util.zip.CRC32

object Manager {

    public val loadedPlugins = mutableListOf<Plugin>()

    private lateinit var pluginsDir: File;
    fun init(context: Context){
        log("Started")
        if(!Python.isStarted()){
            Python.start(AndroidPlatform(context))
        }
        pluginsDir = context.filesDir.resolve("plugins")
        val py = Python.getInstance()
        py.getModule("sys").get("path")?.callAttrThrows("append", pluginsDir.absolutePath)
        val basePlugin = py.getModule("Base").get("Base")
        py.builtins.put("Base", basePlugin)
        val methodhook = py.getModule("Hook").get("MethodHook")
        val replacehook = py.getModule("Hook").get("MethodReplacement")
        py.builtins.put("MethodHook", methodhook)
        py.builtins.put("MethodReplacement", replacehook)
        py.builtins.put("context", context)
        py.builtins.put("hook", HookUtilities())
        py.builtins.put("util", AndroidUtilities())
        runModules()


        var sendContent = Client::class.java.getDeclaredMethod("send", TdApi.Function::class.java, Client.ResultHandler::class.java, Client.ExceptionHandler::class.java)
        XposedBridge.hookMethod(sendContent, object : XC_MethodHook() {
            override fun beforeHookedMethod(param: MethodHookParam?) {
                if (param == null) return;
                val function = param.args[0] as? TdApi.Function<*>
                if(function is TdApi.SendMessage || function is TdApi.SendMessageAlbum){
                    for(plugin in loadedPlugins){
                        if(!plugin.enabled) continue
                        log("Plugin ${plugin.name} is enabled")
                        val hasSendMessage = py.builtins.callAttrThrows("hasattr", plugin.obj, "on_send_message").toBoolean()
                        log("Plugin ${plugin.name} has_send_message: $hasSendMessage")
                        if(hasSendMessage){
							plugin.obj?.callAttrThrows("on_send_message", param.args[0])
                        }
                    }
                }

            }
        })


    }
    fun checkPlugin(id: Int, enabled: Boolean){
        MoexConfig.instance().putBoolean(id.toString(), enabled)
        val plugin = loadedPlugins.find { it.id == id }
        if (plugin != null){
            plugin.enabled = enabled
            if(!plugin.enabled){
                plugin.obj?.callAttrThrows("on_unload")
            }
            else{
                plugin.obj?.callAttrThrows("on_load")
            }

        }
    }


    fun loadPlugin(file: File){
        if(!file.isFile) return
        val py = Python.getInstance()
        val basePlugin = py.getModule("Base").get("Base")
        val inspect = py.getModule("inspect")
        var module: PyObject?
        if(file.extension == "py"){
            val moduleName = file.nameWithoutExtension
            try {
                module = py.getModule(moduleName)
                val pluginId = module.get("__id__")
                val pluginDesc = module.get("__description__")
                if(pluginId == null) return
                if((loadedPlugins.find { it.id == generateId(pluginId.toString()) }) != null) {
                    log("$moduleName is already loaded or has the same id")
                    return
                }
                log("$moduleName has id $pluginId")
                val id = generateId(pluginId.toString())
                val plugin = Plugin(id, moduleName, null, MoexConfig.instance().getBoolean(id.toString(), true));
				module.put("logger", Logger(plugin))


                val members = inspect.callAttrThrows("getmembers", module, inspect.get("isclass")).asList()
                log("Checking $moduleName with $members")
                for(member in members){
                    log("Checking $member")
                    val clazz = member.asList()[1]
                    val isSubclass = py.builtins.callAttrThrows("issubclass", clazz, basePlugin).toBoolean()
                    if(isSubclass){
                        val clazzLoaded = clazz?.call()
                        if (clazzLoaded != null){
                            plugin.obj = clazzLoaded
                            if(plugin.enabled){
                                clazzLoaded.callAttrThrows("on_load")
                                log("Called on_load for $moduleName")
                                log("Loaded $moduleName with id $id")

                            }
                            loadedPlugins.add(plugin)
                        }
                        break
                    }

                }

            }
            catch (e: Exception){
                log("Failed to load $moduleName: ${e.message}")
                return
            }

        }
    }
    fun generateId(moduleName: String): Int{
        val crc = CRC32()
        crc.update(moduleName.toByteArray())
        val id = (crc.value and 0xFFFFFFFF).toInt()
        return id
    }

    fun log(message: String){
        Log.d("PluginManager", message)
    }

    fun deletePlugin(name: String){
        var x = loadedPlugins.find { it.name == name }
        x?.obj?.callAttrThrows("on_unload")
        loadedPlugins.remove(x)
        File(pluginsDir.absolutePath + "/$name.py").delete()
    }
    fun runModules(){
        val py = Python.getInstance()

        var list = pluginsDir.listFiles()
        log( list?.toString() ?: "No files")
        list?.forEach { file ->
            log("Loading $file")
            loadPlugin(file)
        }
        SettingsMoexController.loadedPlugins = loadedPlugins
    }

}