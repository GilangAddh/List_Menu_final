package com.example.week5

import android.os.Bundle
import androidx.recyclerview.widget.RecyclerView
import com.example.week5.data.RestoDatabase
import com.example.week5.data.makanan.Makanan
import com.example.week5.databinding.ActivityMakananBinding

class MakananActivity {
    private var _binding: ActivityMakananBinding? = null
    private val binding get() = _binding!!

    private val STORAGE_PERMISSION_CODE = 102
    private val TAG = "PERMISSION TAG"

    lateinit var makananRecyclerView: RecyclerView
    lateinit var restoDB: RestoDatabase
    lateinit var makananList: ArrayList<Makanan>



}



