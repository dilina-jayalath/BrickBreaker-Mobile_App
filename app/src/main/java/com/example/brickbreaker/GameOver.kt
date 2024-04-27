package com.example.brickbreaker


import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class GameOver : AppCompatActivity() {

    private lateinit var tvPoints: TextView
    private lateinit var tvDuration: TextView
    private lateinit var tvHighScore: TextView
    private lateinit var ivNewHeighest: ImageView


    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.game_over)
        ivNewHeighest = findViewById(R.id.ivNewHeighest)
        tvPoints = findViewById(R.id.tvPoints)
        tvDuration = findViewById(R.id.tvDuration)

        var Newduration = ""
        val points = intent.extras?.getInt("points") ?: 0
        val duration = intent.extras?.getLong("duration") ?: 0

        val minutes = duration / (1000 * 60)
        val seconds = (duration / 1000) % 60

        Newduration = String.format(" %02d:%02d", minutes, seconds)

        tvDuration.text = Newduration
        tvPoints.text = points.toString()

        if (points >  0){
            updateHighScoreLowDuration(points, Newduration )
        }

        displayHighScoreAndLowDuration()

    }

    fun restart(view: View) {

        val gameView = GameView(this)

        setContentView(gameView)

    }

    fun exit(view: View) {
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        finish()
    }

    private fun updateHighScoreLowDuration(points: Int , duration: String  ) {
        val sharedPreferences = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
        val currentLowDuration = sharedPreferences.getString("low_duration", "99:59") // Initial value set to maximum
        val currentHighScore = sharedPreferences.getInt("high_score", 0)

        if (points > currentHighScore ) {
            sharedPreferences.edit().putString("low_duration", duration).apply()
            sharedPreferences.edit().putInt("high_score", points).apply()
        }
    }


    private fun displayHighScoreAndLowDuration() {
        val sharedPreferences = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
        val highScore = sharedPreferences.getInt("high_score", 0)
        val lowDuration = sharedPreferences.getString("low_duration", "99:59") // Initial value set to maximum

        tvHighScore = findViewById(R.id.tvHighScore)

        tvHighScore.text = ": $highScore"

    }






}