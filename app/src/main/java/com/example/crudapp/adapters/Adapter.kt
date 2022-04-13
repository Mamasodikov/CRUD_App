package com.example.crudapp.adapters


import android.view.ViewGroup
import androidx.paging.LoadState
import androidx.paging.LoadStateAdapter


class Adapter(private val retry: () -> Unit) : LoadStateAdapter<FooterViewHolder>() {
    override fun onBindViewHolder(holder: FooterViewHolder, loadState: LoadState) {
        holder.bind(loadState)
    }

    override fun onCreateViewHolder(parent: ViewGroup, loadState: LoadState): FooterViewHolder {
        return FooterViewHolder.create(parent, retry)
    }

}