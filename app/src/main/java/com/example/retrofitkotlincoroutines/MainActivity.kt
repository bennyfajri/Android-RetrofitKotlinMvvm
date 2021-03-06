package com.example.retrofitkotlincoroutines

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.retrofitkotlincoroutines.adapter.MyAdapter
import com.example.retrofitkotlincoroutines.adapter.UserIdAdapter
import com.example.retrofitkotlincoroutines.databinding.ActivityMainBinding
import com.example.retrofitkotlincoroutines.models.Post
import com.example.retrofitkotlincoroutines.models.UserId
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.*

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    lateinit var binding: ActivityMainBinding
    private val myAdapter by lazy {
        MyAdapter()
    }
    private val userIdAdapter by lazy {
        UserIdAdapter()
    }
    private var limit = 0
    private val viewModel: MainViewModel by viewModels()
    private var selectedId = ""
    private var isChecked = false
    private val userIdList = arrayListOf(
        UserId("1"), UserId("2"), UserId("3"),
        UserId("4"), UserId("5"), UserId("6"),
        UserId("7"), UserId("8"), UserId("9"), UserId("10"),
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        getPost()
        setupRecyclerView(viewModel)
        setupRecyclerUserId()
    }

    private fun getPost(userId: Int? = null) {
        viewModel.getCustomPosts(userId = userId, sort = "id", order = "desc")
            .observe(this) { response ->
                when (response) {
                    is Resource.Loading -> {
                        Log.d(TAG, "onCreate: Loading")
                    }
                    is Resource.Success -> {
                        Log.d(TAG, "onCreate: ${response.data}")
                        if (limit < response.data.size) {
                            limit += 10
                        }
                        myAdapter.submitList(response.data.take(limit))
                        userIdAdapter.setData(userIdList)
                        Log.d("response::::", "userId: ${userIdList.distinct()}")
                        Log.d("response::::", "userId: ${userIdList.distinct().size}")
                        binding.progressBar.visibility = View.GONE
                    }
                    is Resource.Error -> {
                        Toast.makeText(applicationContext, "Connection error", Toast.LENGTH_SHORT)
                            .show()
                    }
                }
            }
    }

    private fun setupRecyclerView(viewModel: MainViewModel) {
        binding.rvData.apply {
            adapter = myAdapter
            layoutManager = LinearLayoutManager(this@MainActivity)
            setHasFixedSize(true)
            addOnScrollListener(object : RecyclerView.OnScrollListener() {
                override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                    super.onScrolled(recyclerView, dx, dy)
                    val lnManager =
                        LinearLayoutManager::class.java.cast(recyclerView.layoutManager)
                    val lastVisibleItem = lnManager?.findLastCompletelyVisibleItemPosition()
                    val total = myAdapter.itemCount

                    if (lastVisibleItem!! + 1 >= total) {
                        binding.progressBar.visibility = View.VISIBLE
                        CoroutineScope(Dispatchers.Main).launch {
                            delay(1500)
                            getPost(userId = selectedId.toInt())
                        }
                    }

                    Log.d(TAG, "lastVisible: $lastVisibleItem total: $total")
                }
            })

        }

        myAdapter.setOnITemClickCallback(object : MyAdapter.OnItemClickCallback {
            override fun onItemClicked(data: Post) {
                val i = Intent(this@MainActivity, DetailActivity::class.java)
                i.putExtra(DATA_TAG, data)
                startActivity(i)
            }
        })
    }

    private fun setupRecyclerUserId() {
        binding.rvUserId.apply {
            adapter = userIdAdapter
            layoutManager =
                LinearLayoutManager(this@MainActivity, LinearLayoutManager.HORIZONTAL, false)
            setHasFixedSize(true)
        }
        userIdAdapter.setOnItemClickCallback(object : UserIdAdapter.OnItemClickCallback {
            override fun onItemClicked(data: UserId) {
                if (!isChecked) {
                    selectedId = data.userId
                    Log.d(TAG, "onItemClicked: $selectedId")
                    getPost(selectedId.toInt())
                    isChecked = true
                } else {
                    if (data.userId == selectedId) {
                        isChecked = false
                        selectedId = ""
                    }
                    if (selectedId.isNotBlank()) {
                        Toast.makeText(
                            applicationContext,
                            "Matikan dulu check pada kategori user $selectedId",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }
        })
    }

    companion object {
        const val TAG = "Response::::::"
        const val DATA_TAG = "data"
    }
}