package com.example.gastroandes

import HistoryAdapter
import android.os.Bundle
import android.widget.ImageButton
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class HistoryActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: HistoryAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_history)

        recyclerView = findViewById(R.id.historyRecyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)

        val historyEntries = listOf(
            "Poke - 13/09",
            "Lucille - 12/09",
            "Chick n chips - 10/09",
            "One Burrito - 06/09"
        )

        adapter = HistoryAdapter(historyEntries)
        recyclerView.adapter = adapter

        findViewById<ImageButton>(R.id.backButton).setOnClickListener {
            finish()
        }
    }
}