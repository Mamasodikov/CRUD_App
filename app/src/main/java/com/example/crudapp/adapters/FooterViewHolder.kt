package com.example.crudapp.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import androidx.core.view.isVisible
import androidx.paging.LoadState
import androidx.recyclerview.widget.RecyclerView
import com.example.crudapp.R

class FooterViewHolder(view: View, retry: () -> Unit) : RecyclerView.ViewHolder(view) {

    val errorMsg = view.findViewById<TextView>(R.id.error_msg)
    val progressBar = view.findViewById<ProgressBar>(R.id.progress_bar)
    val btnRetry = view.findViewById<Button>(R.id.retry_button)

    init {
        btnRetry.setOnClickListener {
            retry.invoke()
        }
    }

    fun bind(loadState: LoadState) {
        if (loadState is LoadState.Error) {
            errorMsg.text = "Loading.. Wait.."
        } else {
            progressBar.isVisible = loadState is LoadState.Loading
            btnRetry.isVisible = loadState !is LoadState.Loading
        }
    }

    companion object {
        fun create(parent: ViewGroup, retry: () -> Unit): FooterViewHolder {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_list_footer, parent, false)
            return FooterViewHolder(view, retry)
        }
    }

}
