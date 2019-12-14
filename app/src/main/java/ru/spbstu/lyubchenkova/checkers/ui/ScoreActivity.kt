package ru.sbpstu.lybchenkova.ui

import android.os.Bundle
import android.preference.PreferenceManager.getDefaultSharedPreferences

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.activity_score.*
import kotlinx.android.synthetic.main.score_item.view.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import ru.spbstu.lyubchenkova.checkers.App
import ru.spbstu.lyubchenkova.checkers.R
import ru.spbstu.lyubchenkova.checkers.database.ScoreDao
import ru.spbstu.lyubchenkova.checkers.database.ScoreEntry
import ru.spbstu.lyubchenkova.checkers.ui.BaseActivity

class ScoreActivity : BaseActivity() {
    private val scoreAdapter: ScoreAdapter = ScoreAdapter()
    private lateinit var database: ScoreDao
    override fun getNavigationDrawerID() = R.id.nav_score

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setTheme()
        setContentView(R.layout.activity_score)

        database = App.getInstance().database.scoreDao()

        setupRecycler()
    }

    private fun setupRecycler() {
        score_recycler.adapter = scoreAdapter
        val recyclerLayoutManager = LinearLayoutManager(applicationContext)
        val dividerItemDecoration = DividerItemDecoration(
                score_recycler.context,
                recyclerLayoutManager.orientation
        )
        this@ScoreActivity.runOnUiThread {
            score_recycler.apply {
                setHasFixedSize(true)
                layoutManager = recyclerLayoutManager
                addItemDecoration(dividerItemDecoration)
            }
        }
        GlobalScope.launch {
            val scoreList = database.getAll()
            scoreAdapter.updateScores(*scoreList.toTypedArray())
        }
    }

    private fun setTheme() {
        mSharedPreferences = getDefaultSharedPreferences(this)
        val theme = mSharedPreferences.getInt("theme", 1)
        if (theme == 1) {
            setTheme(R.style.AppTheme_NoActionBar)
        } else {
            setTheme(R.style.AppThemeDark_NoActionBar)
        }
        Log.v("theme", "theme is $theme")
    }
}

class ScoreAdapter :
        RecyclerView.Adapter<ScoreAdapter.ScoreViewHolder>() {

    private val scoreList = mutableListOf<ScoreEntry>()
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
            ScoreViewHolder(
                    LayoutInflater.from(parent.context).inflate(
                            R.layout.score_item,
                            parent,
                            false
                    )
            )

    override fun getItemCount(): Int =
            scoreList.size

    override fun onBindViewHolder(holder: ScoreViewHolder, position: Int) =
            holder.bind(scoreList[position])

    fun updateScores(vararg scoreEntry: ScoreEntry) {
        scoreEntry.forEach {
            scoreList.add(it)
        }
        scoreEntry.sortedByDescending { it.score }
        notifyDataSetChanged()
    }

    inner class ScoreViewHolder(private val view: View) : RecyclerView.ViewHolder(view) {
        fun bind(scoreEntry: ScoreEntry) {
            scoreEntry.apply {
                view.place_text.text = (this@ScoreViewHolder.layoutPosition + 1).toString()
                view.player_name_text.text = scoreEntry.playerName
                view.score_text.text = scoreEntry.score.toString()
            }
        }
    }

}
