package moe.kirao.mgx.plugins.utils

import android.content.Context
import android.os.Handler
import android.os.Looper
import com.chaquo.python.PyObject
import kotlinx.coroutines.GlobalScope
import org.thunderdog.challegram.BaseActivity
import org.thunderdog.challegram.BaseApplication
import org.thunderdog.challegram.Log


class AndroidUtilities() {

	fun runOnUiThread(r: PyObject){
		Log.d("PyLogger", "Running on ui thread")
		Handler(Looper.getMainLooper()).post {
			r.callThrows()
		}

	}
}
