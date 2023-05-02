package com.example.week5.makanan

import android.app.Activity.RESULT_OK
import android.content.ContentValues
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.provider.Settings
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.app.ActivityCompat.startActivityForResult
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil.setContentView
import androidx.lifecycle.lifecycleScope
import com.example.week5.MakananActivity
import com.example.week5.data.RestoDatabase
import com.example.week5.data.makanan.Makanan
import com.example.week5.databinding.EditMakananActivityBinding
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream
import android.Manifest

class EditMakananActivity : AppCompatActivity() {
    private var _binding: EditMakananActivityBinding? = null
    private val binding get() = _binding!!

    private val REQ_CAM = 101
    private var data_gambar: Bitmap? = null
    private var old_foto_dir = ""
    private var new_foto_dir = ""
    private var final_foto_dir = ""

    private var id_makanan: Int = 0

    lateinit var restoDatabase: RestoDatabase
    private val STORAGE_PERMISSION_CODE = 102

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        _binding = EditMakananActivityBinding.inflate(layoutInflater)
        setContentView(binding.root)

        restoDatabase = RestoDatabase(this@EditMakananActivity)

        val intent = intent

        binding.TxtEditNama.setText(intent.getStringExtra("nama_makanan").toString())
        binding.TxtEditJenis.setText(intent.getStringExtra("jenis_makanan").toString())
        binding.TxtEditHarga.setText(intent.getStringExtra("harga_makanan").toString())

        id_makanan = intent.getStringExtra("id").toString().toInt()

        old_foto_dir = intent.getStringExtra("foto_makanan").toString()
        val imgFile = File("${Environment.getExternalStorageDirectory()}/${old_foto_dir}")
        val myBitmap = BitmapFactory.decodeFile(imgFile.absolutePath)
        binding.BtnImgMakanan.setImageBitmap(myBitmap)

        if (!checkPermission()) {
            requestPermission()
        }

        binding.BtnImgMakanan.setOnClickListener {
            openCamera()
        }

        binding.BtnEditMakanan.setOnClickListener{
            editMakanan()
        }

    }

    private fun checkPermission() : Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            Environment.isExternalStorageManager()
        } else {
            val write = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
            val read = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
            write == PackageManager.PERMISSION_GRANTED && read == PackageManager.PERMISSION_GRANTED
        }
    }

    private fun requestPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            try {
                val intent = Intent()
                intent.action = Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION
                val uri = Uri.fromParts("package", this.packageName, null)
                intent.data = uri
            } catch (e:Exception) {
                val intent = Intent()
                intent.action = Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION
            }
        } else {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE),
                STORAGE_PERMISSION_CODE)
        }
    }

    fun saveMediaToStorage(bitmap: Bitmap) : String {
        val filename = "${System.currentTimeMillis()}.jpg"

        var fos: OutputStream? = null
        var image_save = ""

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            this.contentResolver?.also { resolver ->
                val contentValues = ContentValues().apply {
                    put(MediaStore.MediaColumns.DISPLAY_NAME, filename)
                    put(MediaStore.MediaColumns.MIME_TYPE, "image/jpg")
                    put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_PICTURES)
                }
                val imageUri: Uri? =
                    resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
                fos = imageUri?.let {resolver.openOutputStream(it)}
                image_save = "${Environment.DIRECTORY_PICTURES}/${filename}"
            }
        } else {
            val permission = ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)

            if (permission != PackageManager.PERMISSION_GRANTED){
                ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    Manifest.permission.READ_EXTERNAL_STORAGE),
                    STORAGE_PERMISSION_CODE)
            }

            val imageDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
            val image = File(imageDir, filename)
            fos = FileOutputStream(image)

            image_save = "${Environment.DIRECTORY_PICTURES}/${filename}"

        }
        fos?.use {bitmap.compress(Bitmap.CompressFormat.JPEG, 100, it)}
        return image_save
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQ_CAM && resultCode == RESULT_OK){
            data_gambar = data?.extras?.get("data") as Bitmap
            val image_save_uri: String = saveMediaToStorage(data_gambar!!)
            new_foto_dir = image_save_uri
            binding.BtnImgMakanan.setImageBitmap(data_gambar)
        }
    }

    private fun openCamera() {
        Intent(MediaStore.ACTION_IMAGE_CAPTURE).also { intent ->
            this.packageManager?.let {
                intent?.resolveActivity(it).also {
                    startActivityForResult(intent, REQ_CAM)
                }
            }

        }
    }


    private fun editMakanan(){
        val nama_makanan = binding.TxtEditNama.text.toString()
        val jenis_makanan = binding.TxtEditJenis.text.toString()
        val harga_makanan = binding.TxtEditHarga.text.toString()

        if (new_foto_dir != "") {
            final_foto_dir = new_foto_dir
            Log.e("new foto", new_foto_dir)

            val imagesDir =
                Environment.getExternalStoragePublicDirectory("")

            val old_foto_delete = File(imagesDir, old_foto_dir)

            if(old_foto_delete.exists()) {
                if (old_foto_delete.delete()){
                    Log.e("foto final", final_foto_dir)
                }
            }
        }
        else {
            final_foto_dir = old_foto_dir
        }

        lifecycleScope.launch{
            val makanan = Makanan(nama_makanan, jenis_makanan, harga_makanan.toDouble(), final_foto_dir)
            makanan.id = id_makanan
            restoDatabase.getMakananDao().updateMakanan(makanan)
        }

        val intentMakanan = Intent(this, MakananActivity::class.java)
        startActivity(intentMakanan)
    }





}