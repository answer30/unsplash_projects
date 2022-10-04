package com.answere2022.unsplash_projects

import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.answere2022.unsplash_projects.data.Repository
import com.answere2022.unsplash_projects.data.models.PhotoResponse
import com.answere2022.unsplash_projects.databinding.ActivityMainBinding
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {


    private val binding by lazy { ActivityMainBinding.inflate(layoutInflater) }
    private val scope = MainScope()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        initView()
        bindViews()

        fetchRandomPhotos()

    }

    override fun onDestroy() {
        super.onDestroy()
        scope.cancel()
    }

    private fun initView() {
        binding.recyclerView.layoutManager = LinearLayoutManager(this, RecyclerView.VERTICAL, false)
        binding.recyclerView.adapter = PhotoAdapter()

    }

    private fun bindViews() {
        binding.searchEditText.

            //특정 액션에 대해서 리스너 등록해서 핸들링하게 해주는 부분
        setOnEditorActionListener { editText, actionId, event ->


            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                currentFocus?.let { view ->
                    val inputMethodManager =
                        getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager
                    inputMethodManager?.hideSoftInputFromWindow(view.windowToken, 0)


                    view.clearFocus()
                }
                fetchRandomPhotos(editText.text.toString())
            }
            true


        }

        binding.refreshLayout.setOnRefreshListener {
            fetchRandomPhotos(binding.searchEditText.text.toString())
        }

        (binding.recyclerView.adapter as? PhotoAdapter)?.onClickPhoto = { photo ->
            showDownloadDialog(photo)
        }

    }

    //사진 부분 로딩 완료 후 처리
    private fun fetchRandomPhotos(query: String? = null) = scope.launch {

        try {
            Repository.getRandomPhotos(query)?.let { photos ->

                binding.errorDescriptionTextView.visibility = View.GONE
                (binding.recyclerView.adapter as? PhotoAdapter)?.apply {
                    this.photos = photos
                    notifyDataSetChanged()

                }

            }
            binding.recyclerView.visibility = View.VISIBLE
        } catch (exception: Exception) {
            binding.recyclerView.visibility = View.INVISIBLE
            binding.errorDescriptionTextView.visibility = View.VISIBLE
        } finally {


            binding.shimmerLayout.visibility = View.GONE
            binding.refreshLayout.isRefreshing = false


        }


    }

    private fun showDownloadDialog(photo: PhotoResponse) {
        //실제로 다운받게 하는 부분

        AlertDialog.Builder(this)
            .setMessage("이 사진을 저장하시겠습니까")
            .setPositiveButton("저장") { dialog, _ ->
                downloadPhoto(photo.urls?.full)
                dialog.dismiss()


            }
            .setNegativeButton("취소") { dialog, _ ->
                dialog.dismiss()

            }
            .create()
            .show()

    }

    private fun downloadPhoto(photoUrl: String?) {
        photoUrl ?: return



        Glide.with(this)
            .asBitmap()
            .load(photoUrl)
            .diskCacheStrategy(DiskCacheStrategy.NONE)
            .into(
                object : CustomTarget<Bitmap>(SIZE_ORIGINAL, SIZE_ORIGINAL) {

                    //다운로드가 다 완료된 상태
                    override fun onResourceReady(
                        resource: Bitmap,
                        transition: Transition<in Bitmap>?
                    ) {
                        saveBitmapMediaStore(resource)
                    }

                    override fun onLoadStarted(placeholder: Drawable?) {
                        super.onLoadStarted(placeholder)

                        Snackbar.make(
                            binding.root,
                            "다운로드중...", Snackbar.LENGTH_INDEFINITE
                        ).show()
                    }

                    override fun onLoadCleared(placeholder: Drawable?) = Unit


                    override fun onLoadFailed(errorDrawable: Drawable?) {
                        super.onLoadFailed(errorDrawable)
                        Snackbar.make(
                            binding.root,
                            "다운로드중...",
                            Snackbar.LENGTH_SHORT
                        ).show()

                    }


                }

            )

    }

    private fun saveBitmapMediaStore(bitmap: Bitmap) {
        val fillName = "${System.currentTimeMillis()}.jpg"
        val resolver = applicationContext.contentResolver

        //이미지 저장이 안드 10부터 다르기때문에 10기준으로 나눠서 저장
        val imageCollectionUri =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                MediaStore.Images.Media.getContentUri(
                    MediaStore.VOLUME_EXTERNAL_PRIMARY
                )

            } else {
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI


            }

        val imageDetails = ContentValues().apply {
            put(MediaStore.Images.Media.DISPLAY_NAME, fillName)
            put(MediaStore.MediaColumns.MIME_TYPE, "image/jpg")

            //큰이미지 저장시 안드로이드 버전마다 분기처리
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {


                put(MediaStore.Images.Media.IS_PENDING, 1)

            } else {
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI


            }

        }

        //실제로 저장할 이미지 uri ,정보삽입 부분
        val imageUri = resolver.insert(imageCollectionUri, imageDetails)
        //imageUri null값 리턴시
        imageUri ?: return
        resolver.openOutputStream(imageUri).use { outputStream ->
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {

            imageDetails.clear()
            //팬딩값 0으로 다시 처리
            imageDetails.put(MediaStore.Images.Media.IS_PENDING, 0)
            resolver.update(imageUri, imageDetails, null, null)

        }

        Snackbar.make(binding.root, "다운로드완료", Snackbar.LENGTH_SHORT).show()


    }

}
