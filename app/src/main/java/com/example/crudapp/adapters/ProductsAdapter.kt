package com.example.crudapp.adapters

import android.graphics.Color
import android.view.View
import android.view.ViewGroup
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.example.crudapp.models.Product

class ProductsAdapter(
    private val productLongClickInterface: ProductLongClickInterface,
    private val productEditInterface: ProductEditInterface
) : PagingDataAdapter<Product, RecyclerView.ViewHolder>(REPO_COMPORATOR) {
    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val product = getItem(position)
        if (product != null) {
            (holder as ProductsViewHolder).bind(product)

            with(holder) {

                if (product.id != 0) {
                    parent.setBackgroundColor(Color.parseColor("#7EFFAD"))
                } else
                    parent.setBackgroundColor(Color.parseColor("#BCBDBC"))

                prodName.text = product.name_uz.toString()
                prodType.text = product.product_type_id.toString()
                prodCost.text = product.cost.toString()
                prodAddress.text = product.address
                pubDate.text = product.created_date.toString()

                parent.setOnLongClickListener(object : View.OnLongClickListener {
                    override fun onLongClick(p0: View?): Boolean {
                        productLongClickInterface.onActivityLongClick(product)
                        return true
                    }
                })

                editButton.setOnClickListener {
                    productEditInterface.onEditIconClick(product)
                }
            }


        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return ProductsViewHolder.create(parent)
    }

    companion object {
        private val REPO_COMPORATOR = object : DiffUtil.ItemCallback<Product>() {
            override fun areItemsTheSame(oldItem: Product, newItem: Product): Boolean {
                return oldItem.id == newItem.id
            }

            override fun areContentsTheSame(oldItem: Product, newItem: Product): Boolean {
                return oldItem == newItem
            }

        }
    }
}


interface ProductLongClickInterface {

    fun onActivityLongClick(product: Product)
}


interface ProductEditInterface {

    fun onEditIconClick(product: Product)
}
