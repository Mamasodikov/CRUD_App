package com.example.crudapp

import CheckOnlineReceiver
import android.annotation.SuppressLint
import android.app.Dialog
import android.content.ContentValues.TAG
import android.content.Context
import android.content.IntentFilter
import android.content.SharedPreferences
import android.net.ConnectivityManager
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.paging.LoadState
import com.example.crudapp.adapters.Adapter
import com.example.crudapp.adapters.ProductEditInterface
import com.example.crudapp.adapters.ProductLongClickInterface
import com.example.crudapp.adapters.ProductsAdapter
import com.example.crudapp.api.API
import com.example.crudapp.arch.ProductViewModelFactory
import com.example.crudapp.arch.ProductsViewModel
import com.example.crudapp.databinding.ActivityMainBinding
import com.example.crudapp.models.Product
import com.example.crudapp.models.ProductPost
import com.example.crudapp.models.ProductType
import com.example.crudapp.repository.Repository
import com.example.crudapp.room.ProductDatabase
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChangedBy
import kotlinx.coroutines.flow.filter
import retrofit2.HttpException
import java.io.IOError
import java.io.IOException
import java.util.*
import kotlin.collections.HashSet


class MainActivity : AppCompatActivity(),
    ProductLongClickInterface, CheckOnlineReceiver.NetworkStateReceiverListener,
    ProductEditInterface {
    lateinit var bnd: ActivityMainBinding
    lateinit var productsViewModel: ProductsViewModel
    lateinit var addEditDialog: Dialog
    lateinit var view: View
    lateinit var submit: Button
    lateinit var name: EditText
    lateinit var type: Spinner
    lateinit var cost: EditText
    lateinit var address: EditText
    lateinit var adptr: ProductsAdapter
    lateinit var db: ProductDatabase
    var checkOnlineReceiver = CheckOnlineReceiver()
    var hasNetwork = false

    lateinit var pref: SharedPreferences
    lateinit var editor: SharedPreferences.Editor


    @OptIn(DelicateCoroutinesApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        bnd = ActivityMainBinding.inflate(layoutInflater)
        setContentView(bnd.root)

        pref = getSharedPreferences("MyPref", 0)
        editor = pref.edit()

        addEditDialog = Dialog(this)
        view = layoutInflater.inflate(R.layout.add_edit_dialog, null)
        submit = view.findViewById<Button>(R.id.submit)
        name = view.findViewById<EditText>(R.id.prodNameEdit)
        type = view.findViewById<Spinner>(R.id.prodTypeEdit)
        cost = view.findViewById<EditText>(R.id.prodCostEdit)
        address = view.findViewById<EditText>(R.id.prodAddressEdit)


        startNetworkBroadcastReceiver(this)


        val api = API()
        db = ProductDatabase(this)
        val daoType = db.productTypeDao()
        var typeList: List<ProductType> = listOfNotNull()
        val repo = Repository(api, db)
        val factory = ProductViewModelFactory(repo)
        productsViewModel = ViewModelProvider(this, factory).get(ProductsViewModel::class.java)


        GlobalScope.launch(Dispatchers.Default) {

            val list = daoType.getAllProductType()
            if (list.isEmpty()) {
                //Getting All Product Types
                val retroInstance = API()

                try {
                    typeList = retroInstance.getProductTypes()
                    daoType.insertAll(typeList)
                } catch (e: HttpException) {
                    e.printStackTrace()
                } catch (e: IOException) {
                    e.printStackTrace()
                } catch (e: IOError) {
                    e.printStackTrace()
                }

            } else
                typeList = daoType.getAllProductType()


            val stringList: MutableList<String> = mutableListOf()
            for (type in typeList) {
                stringList.add(type.name_uz)
            }

            adptr.typeList = typeList
            type.adapter = ArrayAdapter(
                this@MainActivity,
                android.R.layout.simple_spinner_dropdown_item,
                stringList
            )
        }


        adptr = ProductsAdapter(this, this, typeList)

        bnd.rvProducts.setHasFixedSize(true)
        bnd.swipeRefresh.setOnRefreshListener {
            if (hasNetwork) {
                adptr.refresh()
                bnd.swipeRefresh.isRefreshing = false
            } else
                bnd.swipeRefresh.isRefreshing = false
        }

        bnd.fabButton.setOnClickListener {

            name.text.clear()
            cost.text.clear()
            address.text.clear()

            addEditDialog.setContentView(view)
            addEditDialog.setCancelable(true)
            addEditDialog.show()


            submit.setOnClickListener {

                if (name.text.isNotEmpty() && cost.text.isNotEmpty() && address.text.isNotEmpty()) {
                    val selected = type.selectedItem as String
                    var currentID: Int = 0

                    for (t in typeList) {
                        if (selected == t.name_uz)
                            currentID = t.id
                    }

                    GlobalScope.launch {


                        if (!hasNetwork) {
                            val dao = db.productsDao()
                            dao.insert(
                                Product(
                                    address.text.toString(),
                                    cost.text.toString().toInt(),
                                    Date().time,
                                    name_uz = name.text.toString(),
                                    product_type_id = currentID,
                                    id = 0
                                )
                            )

//                    Log.d(TAG, "Count: $count")
                        } else {

                            val retroInstance = API()

                            try {
                                retroInstance.postData(
                                    ProductPost(
                                        address.text.toString(),
                                        cost.text.toString().toInt(),
                                        Date().time,
                                        name_uz = name.text.toString(),
                                        product_type_id = currentID
                                    )
                                )

                            } catch (e: HttpException) {
                                e.printStackTrace()
                            } catch (e: IndexOutOfBoundsException) {
                                e.printStackTrace()
                            } catch (e: IOException) {
                                e.printStackTrace()
                            }

                        }
                        adptr.refresh()
                    }

                    addEditDialog.dismiss()
                } else Toast.makeText(this, "Pls, fill th fileds", Toast.LENGTH_SHORT).show()
            }

        }

        initAdapter()
        loadData()

        bnd.btnRefresh.setOnClickListener {
            adptr.retry()
        }

    }

    private fun loadData() {

        lifecycleScope.launch {
            productsViewModel.getModelProducts().collectLatest { pagingData ->
                adptr.submitData(pagingData)
            }
        }

        lifecycleScope.launch {
            adptr.loadStateFlow
                .distinctUntilChangedBy { it.refresh }
                .filter { it.refresh is LoadState.NotLoading }
                .collect { bnd.rvProducts.scrollToPosition(0) }
        }
    }


    private fun initAdapter() {
        bnd.rvProducts.adapter = adptr.withLoadStateHeaderAndFooter(
            header = Adapter { adptr.retry() },
            footer = Adapter { adptr.retry() }
        )

        adptr.addLoadStateListener { loadState ->
            // here is some issue: Abnormal blinking!!

//            bnd.rvProducts.isVisible = loadState.source.refresh is LoadState.NotLoading
//            bnd.progressBar.isVisible = loadState.source.refresh is LoadState.Loading

            bnd.btnRefresh.isVisible = loadState.source.refresh is LoadState.Error
        }
    }

    @SuppressLint("NotifyDataSetChanged", "MutatingSharedPrefs")
    @OptIn(DelicateCoroutinesApi::class)
    override fun onActivityLongClick(product: Product) {
        GlobalScope.launch(Dispatchers.Main) {
            if (hasNetwork) {
                val retroInstance = API()

                try {
                    val response = retroInstance.deleteData(product.id)
                    adptr.refresh()
                    Toast.makeText(this@MainActivity, "Deleted", Toast.LENGTH_SHORT).show()
                } catch (e: HttpException) {
                    e.printStackTrace()
                } catch (e: IndexOutOfBoundsException) {
                    e.printStackTrace()
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            } else {

                val dao = db.productsDao()
                dao.delete(product)

                val size = pref.getStringSet("ids", HashSet<String>())?.size

                if (product.id != 0) {
                    if (size == null) {

                        Log.d(TAG, "onActivityLongClick: Null ")
                        val set = mutableSetOf<String>()
                        set.add(product.id.toString())
                        editor.remove("ids")
                        editor.putStringSet("ids", set)
                        editor.apply()
                        editor.commit()
                    } else {

                        val set = HashSet<String>(pref.getStringSet("ids", HashSet()))
                        Log.d(TAG, "onActivityLongClick: $size ")
                        set.add(product.id.toString())
                        editor.putStringSet("ids", set)
                        editor.apply()
                        editor.commit()
                    }
                }

            }
        }
    }

    @OptIn(DelicateCoroutinesApi::class)
    override fun networkAvailable() {
        Toast.makeText(this, "Online", Toast.LENGTH_SHORT).show()
        hasNetwork = true

        db = ProductDatabase(this)
        val dao = db.productsDao()
        val daoType = db.productTypeDao()
        var offineProducts: List<Product>
        val offlineDelSize = pref.getStringSet("ids", HashSet<String>())?.size

        GlobalScope.launch(Dispatchers.IO) {

            //Getting All Product Types
            val retroInstance = API()

            try {
                val typeList = retroInstance.getProductTypes()
                daoType.insertAll(typeList)
            } catch (e: HttpException) {
                e.printStackTrace()
            } catch (e: IOException) {
                e.printStackTrace()
            } catch (e: IOError) {
                e.printStackTrace()
            }

        }

        GlobalScope.launch {

            offineProducts = dao.getAllOfflineProducts()
            val retroInstance = API()


//     Adding Offline added and cached items to the network

            if (offineProducts.size > 0) {


                for (p in offineProducts) {
                    Log.d(TAG, "networkAvailable: Here I'm")
                    try {
                        retroInstance.postData(
                            ProductPost(
                                p.address,
                                p.cost,
                                created_date = p.created_date,
                                name_uz = p.name_uz,
                                product_type_id = p.product_type_id
                            )
                        )

                    } catch (e: HttpException) {
                        e.printStackTrace()
                    } catch (e: IndexOutOfBoundsException) {
                        e.printStackTrace()
                    } catch (e: IOException) {
                        e.printStackTrace()
                    }
                }

            }

//     Deleting Offline deleted and cached items from network

            if (offlineDelSize != null) {
                val set = pref.getStringSet("ids", HashSet())
                Log.d(TAG, "networkAvailable: Deletable Items: $set")

                for (s in set!!) {
                    val response = retroInstance.deleteData(s.toInt())
                }
            }

            adptr.refresh()
        }
    }


    override fun networkUnavailable() {
        Toast.makeText(this, "Offline", Toast.LENGTH_SHORT).show()
        hasNetwork = false
    }


    fun startNetworkBroadcastReceiver(currentContext: Context) {
        checkOnlineReceiver = CheckOnlineReceiver()
        checkOnlineReceiver.addListener(currentContext as CheckOnlineReceiver.NetworkStateReceiverListener)
        registerNetworkBroadcastReceiver(currentContext)
    }

    fun registerNetworkBroadcastReceiver(currentContext: Context) {
        currentContext.registerReceiver(
            checkOnlineReceiver,
            IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION)
        )
    }

    fun unregisterNetworkBroadcastReceiver(currentContext: Context) {
        currentContext.unregisterReceiver(checkOnlineReceiver)
    }

    override fun onPause() {
        unregisterNetworkBroadcastReceiver(this)
        super.onPause()
    }

    override fun onResume() {
        registerNetworkBroadcastReceiver(this)
        super.onResume()
    }

    @OptIn(DelicateCoroutinesApi::class)
    override fun onEditIconClick(product: Product) {
        if (hasNetwork) {
            name.setText(product.name_uz)
            cost.setText(product.cost.toString())
            address.setText(product.address)
            addEditDialog.setContentView(view)
            addEditDialog.setCancelable(true)
            addEditDialog.show()


            db = ProductDatabase(this)
            val daoType = db.productTypeDao()
            var typeList: List<ProductType> = listOfNotNull()


            GlobalScope.launch(Dispatchers.IO) {
                typeList = daoType.getAllProductType()
                val stringList: MutableList<String> = mutableListOf()

                for (type in typeList) {
                    stringList.add(type.name_uz)
                }

                type.adapter = ArrayAdapter(
                    this@MainActivity,
                    android.R.layout.simple_spinner_dropdown_item,
                    stringList
                )
                type.setSelection(product.product_type_id - 1)
            }


            submit.setOnClickListener {

                if (name.text.isNotEmpty() && cost.text.isNotEmpty() && address.text.isNotEmpty()) {
                    val selected = type.selectedItem as String
                    var currentID: Int = 0

                    for (t in typeList) {
                        if (selected == t.name_uz)
                            currentID = t.id
                    }


                    GlobalScope.launch {

                        val retroInstance = API()

                        try {
                            retroInstance.updateData(
                                Product(
                                    address.text.toString(),
                                    cost.text.toString().toInt(),
                                    Date().time,
                                    name_uz = name.text.toString(),
                                    product_type_id = currentID, id = product.id
                                )
                            )

                        } catch (e: HttpException) {
                            e.printStackTrace()
                        } catch (e: IndexOutOfBoundsException) {
                            e.printStackTrace()
                        } catch (e: IOException) {
                            e.printStackTrace()
                        }
                        adptr.refresh()
                    }

                    addEditDialog.dismiss()
                } else Toast.makeText(this, "Pls, fill th fileds", Toast.LENGTH_SHORT).show()
            }

        } else Toast.makeText(this, "Please, Connect internet for Editing!", Toast.LENGTH_LONG)
            .show()
    }
}