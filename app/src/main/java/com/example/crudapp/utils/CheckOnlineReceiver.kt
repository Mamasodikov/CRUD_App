import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.NetworkInfo
import android.util.Log


class CheckOnlineReceiver : BroadcastReceiver() {
    protected var listeners: MutableList<NetworkStateReceiverListener>
    protected var connected: Boolean?
    private val TAG = "NetworkStateReceiver"
    override fun onReceive(context: Context, intent: Intent?) {
        Log.i(TAG, "Intent broadcast received")
        if (intent == null || intent.extras == null) return
        val manager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val networkInfo = manager.activeNetworkInfo
        if (networkInfo != null && networkInfo.state == NetworkInfo.State.CONNECTED) {
            connected = true
        } else if (intent.getBooleanExtra(
                ConnectivityManager.EXTRA_NO_CONNECTIVITY,
                java.lang.Boolean.FALSE
            )
        ) {    //Boolean that indicates whether there is a complete lack of connectivity
            connected = false
        }
        notifyStateToAll()
    }

    private fun notifyStateToAll() {
        Log.i(TAG, "Notifying state to " + listeners.size + " listener(s)")
        for (eachNetworkStateReceiverListener in listeners) notifyState(
            eachNetworkStateReceiverListener
        )
    }

    private fun notifyState(networkStateReceiverListener: NetworkStateReceiverListener?) {
        if (connected == null || networkStateReceiverListener == null) return
        if (connected == true) {
            networkStateReceiverListener.networkAvailable()
        } else {
            networkStateReceiverListener.networkUnavailable()
        }
    }

    fun addListener(networkStateReceiverListener: NetworkStateReceiverListener) {
        Log.i(
            TAG,
            "addListener() - listeners.add(networkStateReceiverListener) + notifyState(networkStateReceiverListener);"
        )
        listeners.add(networkStateReceiverListener)
        notifyState(networkStateReceiverListener)
    }

    fun removeListener(networkStateReceiverListener: NetworkStateReceiverListener) {
        listeners.remove(networkStateReceiverListener)
    }

    interface NetworkStateReceiverListener {
        fun networkAvailable()
        fun networkUnavailable()
    }

    init {
        listeners = ArrayList()
        connected = null
    }
}