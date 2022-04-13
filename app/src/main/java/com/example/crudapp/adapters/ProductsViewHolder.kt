package com.example.crudapp.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.example.crudapp.R
import com.example.crudapp.models.Product

class ProductsViewHolder(view: View) : RecyclerView.ViewHolder(view) {

    private val product: Product? = null

    val prodName = view.findViewById<TextView>(R.id.prodName)
    val prodType = view.findViewById<TextView>(R.id.prodType)
    val prodCost = view.findViewById<TextView>(R.id.prodCost)
    val prodAddress = view.findViewById<TextView>(R.id.prodAddress)
    val pubDate = view.findViewById<TextView>(R.id.pubDate)
    val parent = view.findViewById<CardView>(R.id.productBody)
    val editButton = view.findViewById<ImageButton>(R.id.btnEdit)

    fun bind(product: Product?) {
        if (product == null) {
        } else showData(product)
    }

    private fun showData(product: Product) {

    }

    companion object {
        fun create(parent: ViewGroup): ProductsViewHolder {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.product_item, parent, false)
            return ProductsViewHolder(view)
        }
    }


}