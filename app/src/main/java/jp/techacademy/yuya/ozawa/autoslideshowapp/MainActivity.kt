package jp.techacademy.yuya.ozawa.autoslideshowapp

import android.content.ContentUris
import android.content.pm.PackageManager
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import jp.techacademy.yuya.ozawa.autoslideshowapp.databinding.ActivityMainBinding
import java.util.*

class MainActivity : AppCompatActivity() {


    private lateinit var binding: ActivityMainBinding

    private var timer: Timer? = null



    private val PERMISSIONS_REQUEST_CODE = 100

    // APIレベルによって許可が必要なパーミッションを切り替える
    private val readImagesPermission =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) android.Manifest.permission.READ_MEDIA_IMAGES
        else android.Manifest.permission.READ_EXTERNAL_STORAGE

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        // パーミッションの許可状態を確認する
        if (checkSelfPermission(readImagesPermission) == PackageManager.PERMISSION_GRANTED) {
            // 許可されている
            getContentsInfo()
        } else {
            // 許可されていないので許可ダイアログを表示する
            requestPermissions(
                arrayOf(readImagesPermission),
                PERMISSIONS_REQUEST_CODE
            )
        }
        binding.btnNext.setOnClickListener {
            moveNext()
        }
        binding.btnBack.setOnClickListener {
            movePrevious()
        }
        binding.btnPlay.setOnClickListener {
            if (binding.btnPlay.text == "再生"){
                binding.btnNext.isEnabled = false
                binding.btnBack.isEnabled = false
                binding.btnPlay.text = "停止"
                timer = Timer()
                timer?.scheduleAtFixedRate(object : TimerTask() {
                    override fun run() {
                        runOnUiThread {
                            moveNext()
                        }
                    }
                }, 2000, 2000)
            } else {
                binding.btnPlay.text = "再生"
                binding.btnNext.isEnabled = true
                binding.btnBack.isEnabled = true
                timer?.cancel()
            }
        }
    }




    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            PERMISSIONS_REQUEST_CODE ->
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    getContentsInfo()

                }else{
                    binding.btnNext.isEnabled = false
                    binding.btnBack.isEnabled = false
                    binding.btnPlay.isEnabled = false
                }
        }
    }

    private fun getContentsInfo() {// 画像の情報を取得する

        val resolver = contentResolver //データを参照するためのクラス
        val cursor = resolver.query( //queryを使って情報の取得
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI, // データの種類
            null, // 項目（null = 全項目）
            null, // フィルタ条件（null = フィルタなし）
            null, // フィルタ用パラメータ
            null // ソート (nullソートなし）
        )
        //Cursorとはデータベース上の検索結果を格納するもの
        if (cursor!!.moveToFirst()) {
            // indexからIDを取得し、そのIDから画像のURIを取得する
            val fieldIndex = cursor.getColumnIndex(MediaStore.Images.Media._ID)
            val id = cursor.getLong(fieldIndex)
            val imageUri =
                ContentUris.withAppendedId(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, id)

            binding.imageView.setImageURI(imageUri)
        }
        cursor.close()
    }
    private var currentPosition: Int = 0

    private fun moveNext() {
        val resolver = contentResolver
        val cursor = resolver.query(
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
            null,
            null,
            null,
            null
        )

        if (cursor!!.moveToPosition(currentPosition + 1)) {
            val fieldIndex = cursor.getColumnIndex(MediaStore.Images.Media._ID)
            val id = cursor.getLong(fieldIndex)
            val imageUri =
                ContentUris.withAppendedId(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, id)
            binding.imageView.setImageURI(imageUri)

            currentPosition++
        } else {

            cursor.moveToFirst()
            val fieldIndex = cursor.getColumnIndex(MediaStore.Images.Media._ID)
            val id = cursor.getLong(fieldIndex)
            val imageUri =
                ContentUris.withAppendedId(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, id)
            binding.imageView.setImageURI(imageUri)

            currentPosition = 0
        }

        cursor.close()
    }



    private fun movePrevious() {
        val resolver = contentResolver
        val cursor = resolver.query(
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
            null,
            null,
            null,
            null
        )

        if (cursor!!.moveToPosition(currentPosition - 1)) {
            val fieldIndex = cursor.getColumnIndex(MediaStore.Images.Media._ID)
            val id = cursor.getLong(fieldIndex)
            val imageUri =
                ContentUris.withAppendedId(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, id)
            binding.imageView.setImageURI(imageUri)

            currentPosition--
        } else {

            cursor.moveToLast()
            val fieldIndex = cursor.getColumnIndex(MediaStore.Images.Media._ID)
            val id = cursor.getLong(fieldIndex)
            val imageUri =
                ContentUris.withAppendedId(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, id)
            binding.imageView.setImageURI(imageUri)

            currentPosition = cursor.count - 1
        }

        cursor.close()
    }



}
