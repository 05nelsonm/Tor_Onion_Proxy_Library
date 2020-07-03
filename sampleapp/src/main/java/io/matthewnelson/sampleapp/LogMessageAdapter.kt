package io.matthewnelson.sampleapp

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class LogMessageAdapter(
    context: Context,
    lifecycleOwner: LifecycleOwner,
    recyclerView: RecyclerView
): RecyclerView.Adapter<LogMessageAdapter.LogMessageHolder>(){

    companion object {
        private val logMessageList = mutableListOf<String>()
        private const val maxMessages = 200

        private val liveNotifyInserted = MutableLiveData(false)
        private fun observeLiveNotifyInserted(): LiveData<Boolean> {
            liveNotifyInserted.value = null
            return liveNotifyInserted
        }

        private val liveNotifyRemoved = MutableLiveData(false)
        private fun observeLiveNotifyRemoved(): LiveData<Boolean> {
            liveNotifyRemoved.value = null
            return liveNotifyRemoved
        }

        fun addLogMessage(msg: String) {
            logMessageList.add(msg)
            if (liveNotifyInserted.hasActiveObservers()) {
                liveNotifyInserted.value = liveNotifyInserted.value != true
            }
            if (logMessageList.size > maxMessages) {
                removeFirstLogMessage()
            }
        }

        private fun removeFirstLogMessage() {
            logMessageList.removeAt(0)
            if (liveNotifyRemoved.hasActiveObservers()) {
                liveNotifyRemoved.value = liveNotifyRemoved.value != true
            }
        }
    }

    init {
        recyclerView.apply {
            setHasFixedSize(true)
            layoutManager = LinearLayoutManager(context)
            adapter = this@LogMessageAdapter
        }

        observeLiveNotifyInserted().observe(lifecycleOwner, Observer {
            if (it != null) {
                this.notifyItemInserted(logMessageList.size - 1)
            }
        })

        observeLiveNotifyRemoved().observe(lifecycleOwner, Observer {
            if (it != null) {
                this.notifyItemRemoved(0)
            }
        })
    }

    inner class LogMessageHolder(val textView: TextView): RecyclerView.ViewHolder(textView)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LogMessageHolder {
        val textView = LayoutInflater.from(parent.context)
            .inflate(R.layout.log_message, parent, false) as TextView
        return LogMessageHolder(textView)
    }

    override fun getItemCount(): Int =
        logMessageList.size

    override fun onBindViewHolder(holder: LogMessageHolder, position: Int) {
        holder.textView.text = logMessageList[position]
    }

}