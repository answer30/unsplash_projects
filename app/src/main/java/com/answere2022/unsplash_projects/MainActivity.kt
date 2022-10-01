package com.answere2022.unsplash_projects

import android.content.Context
import android.os.Bundle
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.answere2022.unsplash_projects.data.Repository
import com.answere2022.unsplash_projects.databinding.ActivityMainBinding
import com.answere2022.unsplash_projects.PhotoAdapter
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

    }


    private fun fetchRandomPhotos(query: String? = null) = scope.launch {
        Repository.getRandomPhotos(query)?.let { photos ->
            (binding.recyclerView.adapter as? PhotoAdapter)?.apply {
                this.photos = photos
                notifyDataSetChanged()
            }

            binding.refreshLayout.isRefreshing = false

        }

    }
}