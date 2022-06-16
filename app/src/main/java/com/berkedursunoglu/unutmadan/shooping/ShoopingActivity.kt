package com.berkedursunoglu.unutmadan.shooping

import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.berkedursunoglu.unutmadan.R
import com.berkedursunoglu.unutmadan.databinding.ActivityShoopingBinding

class ShoopingActivity : AppCompatActivity() {

    private lateinit var dataBinding: ActivityShoopingBinding
    private lateinit var viewModel: ShoopingActivityViewModel
    private lateinit var rv: ShoopingRecyclerView
    private lateinit var guideShared: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        dataBinding = DataBindingUtil.setContentView(this, R.layout.activity_shooping)
        viewModel = ViewModelProvider(this)[ShoopingActivityViewModel::class.java]
        dataBinding.shoopingRecyclerView.layoutManager = LinearLayoutManager(this)
        setSupportActionBar(dataBinding.shopToolbar)
        val guidenote = this.getSharedPreferences("guideShared", MODE_PRIVATE)
        val shop = guidenote.getInt("shopguide",0)
        if (shop == 1){
            dataBinding.shoopguideimage.visibility = View.GONE
        }

        dataFromRoom()
        dataBinding.shoopingAddButton.setOnClickListener {
            addShopItem()
            dataBinding.shoopguideimage.visibility = View.GONE
            guideShared()
        }


    }

    private fun addShopItem() {
        val shopItemEditText = dataBinding.shoopingAddEdittext.text.toString()
        if (shopItemEditText != "") {
            viewModel.addShoopingItem(this, shopItemEditText)
            dataBinding.shoopingAddEdittext.text.clear()
            dataFromRoom()
        }

    }

    private fun dataFromRoom() {
        viewModel.getAllDataFromRoomDatabase(this)
        viewModel.shopList.observe(this) {
            rv = ShoopingRecyclerView(it)
            dataBinding.shoopingRecyclerView.adapter = rv
        }
    }

    private fun deleteAll() {
        viewModel.deleteAll(this)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        val inflater: MenuInflater = menuInflater
        inflater.inflate(R.menu.shooping_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_trash -> {
                var alart = AlertDialog.Builder(this@ShoopingActivity)
                alart.setTitle("Alışveriş Listesi")
                alart.setMessage("Tüm liste silinecektir bunu yapmak istiyor musunuz?")
                alart.setPositiveButton("Evet"){a,b->
                    deleteAll()
                    dataFromRoom()
                }
                alart.setNegativeButton("Hayır"){a,b->

                }
                alart.show()
                true
            }
            else -> return false
        }
    }

    private fun guideShared(){
        guideShared = this.getSharedPreferences("guideShared", AppCompatActivity.MODE_PRIVATE)
        val guidEdit = guideShared.edit()
        guidEdit.putInt("shopguide",1).apply()
    }




}