package com.example.brickbreaker

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Point
import android.media.MediaPlayer
import android.os.Handler
import android.view.Display
import android.view.MotionEvent
import android.view.View

import java.util.Random

class GameView(context: Context) : View(context) {

    private var ballX: Int = 0
    private var ballY: Int = 0
    private val velocity = Velocity(25, 32)
    private val handler: Handler = Handler()
    private val UPDATE_MILLIS: Long = 30
    private val textPaint: Paint = Paint()
    private val healthPaint: Paint = Paint()
    private val brickPaint: Paint = Paint()
    private val TEXT_SIZE: Float = 100f
    private var paddleX: Float = 0f
    private var paddleY: Float = 0f
    private var oldX: Float = 0f
    private var oldPaddleX: Float = 0f
    private var points: Int = 0
    private var life: Int = 3
    private lateinit var ball: Bitmap
    private lateinit var paddle: Bitmap
    private var dWidth: Int = 0
    private var dHeight: Int = 0
    private var ballWidth: Int = 0
    private var ballHeight: Int = 0
    private lateinit var mpHit: MediaPlayer
    private lateinit var mpMiss: MediaPlayer
    private lateinit var mpBreak: MediaPlayer
    private lateinit var random: Random
    private val bricks = mutableListOf<Brick>()
    private var numBricks: Int = 0
    private var brokenBricks: Int = 0


    private var startTime: Long = 0
    private var elapsedTime: Long = 0
    private var duration = ""


    private val NEW_BRICK_ROW_DELAY: Long = 5000
    private val addBrickRowHandler: Handler = Handler()
    private val addBrickRowRunnable: Runnable = object : Runnable {
        override fun run() {
            addBrickRow()
            addBrickRowHandler.postDelayed(this, NEW_BRICK_ROW_DELAY)
        }
    }

    private var gameOver: Boolean = false
    private val runnable: Runnable = object : Runnable {
        override fun run() {
            invalidate()
        }
    }

    init {

        startTime = System.currentTimeMillis()

        addBrickRowHandler.postDelayed(addBrickRowRunnable, NEW_BRICK_ROW_DELAY)

        ball = BitmapFactory.decodeResource(resources, R.drawable.ball)
        paddle = BitmapFactory.decodeResource(resources, R.drawable.paddle)
        textPaint.color = Color.RED
        textPaint.textSize = TEXT_SIZE
        textPaint.textAlign = Paint.Align.LEFT
        healthPaint.color = Color.GREEN
        brickPaint.color = Color.BLUE

        val display: Display = (context as Activity).windowManager.defaultDisplay
        val size = Point()
        display.getSize(size)
        dWidth = size.x
        dHeight = size.y
        random = Random()
        ballX = random.nextInt(dWidth - 50)
        ballY = dHeight / 3
        paddleY = (dHeight * 4) / 5.toFloat()
        paddleX = dWidth / 2 - paddle.width / 2.toFloat()
        ballWidth = ball.width
        ballHeight = ball.height
        createBricks()
    }

    private fun addBrickRow() {
        // Shift existing rows downwards
        for (i in numBricks - 1 downTo 0) {
            bricks.add(bricks[i].copy(row = bricks[i].row + 1))
        }
        // Generate new row of bricks
        val brickWidth: Int = dWidth / 8
        val brickHeight: Int = dHeight / 16
        for (column in 0 until 8) {
            bricks.add(Brick(0, column, brickWidth, brickHeight)) // Create new bricks for the top row
        }
        numBricks += 8 // Increase the total number of bricks
    }


    private fun createBricks() {

        val brickWidth: Int = dWidth / 8
        val brickHeight: Int = dHeight / 16
        for (column in 0 until 8) {
            for (row in 0 until 3) {
                bricks.add(Brick(row, column, brickWidth, brickHeight)) // Error here
            }
        }
        numBricks = bricks.size // Set the total number of bricks
    }


    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        canvas.drawColor(Color.BLACK)
        ballX += velocity.x
        ballY += velocity.y

        if (ballX >= dWidth - ball.width || ballX <= 0) {
            velocity.x *= -1
        }

        if (ballY <= 0) {
            velocity.y *= -1
        }

        if (ballY > paddleY + paddle.height) {
            ballX = (1 + random.nextInt(dWidth - ball.width - 1))
            ballY = dHeight / 3
            velocity.x = xVelocity()
            velocity.y = 32
            life--
            if (life == 0) {
                gameOver = true
                launchGameOver()
            }
        }

        if (ballX + ball.width >= paddleX && ballX <= paddleX + paddle.width && ballY + ball.height >= paddleY && ballY + ball.height <= paddleY + paddle.height) {
            velocity.x += 1
            velocity.y = (velocity.y + 1) * -1
        }
        canvas.drawBitmap(ball, ballX.toFloat(), ballY.toFloat(), null)
        canvas.drawBitmap(paddle, paddleX, paddleY, null)

        for (i in 0 until numBricks) {
            if (bricks[i].getVisibility()) {
                canvas.drawRect((bricks[i].column * bricks[i].width + 1).toFloat(),
                    (bricks[i].row * bricks[i].height + 1).toFloat(),
                    (bricks[i].column * bricks[i].width + bricks[i].width - 1).toFloat(),
                    (bricks[i].row * bricks[i].height + bricks[i].height - 1).toFloat(), brickPaint)
            }
        }



        // Calculate elapsed time
        elapsedTime = System.currentTimeMillis() - startTime
        val minutes = elapsedTime / (1000 * 60)
        val seconds = (elapsedTime / 1000) % 60
        duration = String.format("%02d:%02d", minutes, seconds)

        // Draw duration text on canvas
        textPaint.color = Color.GREEN
        textPaint.textSize = 60f
        canvas.drawText( "$duration",   dWidth-200.toFloat(), dHeight-dHeight+200.toFloat(), textPaint)




        textPaint.color = Color.RED
        textPaint.textSize = TEXT_SIZE
        canvas.drawText("" + points, 20f, TEXT_SIZE, textPaint)
        if (life == 2) {
            healthPaint.color = Color.YELLOW
        } else if (life == 1) {
            healthPaint.color = Color.RED
        }
        canvas.drawRect(dWidth - 200.toFloat(), 30.toFloat(), dWidth - 200 + 60 * life.toFloat(), 80.toFloat(), healthPaint)

        for (i in 0 until numBricks) {
            if (bricks[i].getVisibility()) {
                if (ballX + ballWidth >= bricks[i].column * bricks[i].width
                    && ballX <= bricks[i].column * bricks[i].width + bricks[i].width
                    && ballY <= bricks[i].row * bricks[i].height + bricks[i].height
                    && ballY >= bricks[i].row * bricks[i].height) {
                    velocity.y = (velocity.y + 1) * -1
                    bricks[i].setVisible()
                    points += 10
                    brokenBricks++
//                    if (brokenBricks == 24) {
//                        launchGameOver()
//                    }
                }
            }
        }


        if (brokenBricks == numBricks) {
            gameOver = true
        }
        if (!gameOver) {
            handler.postDelayed(runnable, UPDATE_MILLIS)
        }
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        val touchX: Float = event.x
        val touchY: Float = event.y

        if (touchY >= paddleY) {
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    oldX = event.x
                    oldPaddleX = paddleX
                }
                MotionEvent.ACTION_MOVE -> {
                    val shift: Float = oldX - touchX
                    val newPaddleX: Float = oldPaddleX - shift

                    paddleX = when {
                        newPaddleX <= 0 -> 0f
                        newPaddleX >= dWidth - paddle.width -> (dWidth - paddle.width).toFloat()
                        else -> newPaddleX
                    }
                }
            }
        }
        return true
    }

    private fun launchGameOver() {
        handler.removeCallbacksAndMessages(null)
        val intent = Intent(context, GameOver::class.java)
        intent.putExtra("points", points)
        intent.putExtra("duration", elapsedTime)
        context.startActivity(intent)
        (context as Activity).finish()

    }


    private fun xVelocity(): Int {
        val values: IntArray = intArrayOf(-35, -30, -25, 25, 30, 35)
        val index: Int = random.nextInt(6)
        return values[index]
    }
}
