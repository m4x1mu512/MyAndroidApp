package com.example.myandroidapp

import android.content.res.ColorStateList
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.content.res.AppCompatResources
import com.example.myandroidapp.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val engine = GameEngine()
    private val cells = mutableListOf<ImageView>()
    private val handler = Handler(Looper.getMainLooper())

    private var vsComputer = true
    private var scoreX = 0
    private var scoreO = 0
    private var gameFinished = false
    private var aiThinking = false

    private val aiTurn = Runnable {
        aiThinking = false
        performAiTurn()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupCells()
        restoreState(savedInstanceState)
        setupListeners()
        render()
        // Если экран пересоздали в момент, когда ход был за компьютером,
        // партию нужно «доиграть», иначе игра зависнет.
        maybeScheduleAi()
    }

    private fun setupCells() {
        cells += listOf(
            binding.cell0, binding.cell1, binding.cell2,
            binding.cell3, binding.cell4, binding.cell5,
            binding.cell6, binding.cell7, binding.cell8
        )
        cells.forEachIndexed { index, cell ->
            cell.setOnClickListener { onCellClicked(index) }
        }
    }

    private fun setupListeners() {
        binding.switchMode.setOnCheckedChangeListener { _, isChecked ->
            if (vsComputer == isChecked) return@setOnCheckedChangeListener
            vsComputer = isChecked
            startNewGame()
        }
        binding.buttonNewGame.setOnClickListener { startNewGame() }
        binding.buttonResetScore.setOnClickListener {
            scoreX = 0
            scoreO = 0
            renderScore()
        }
    }

    private fun onCellClicked(index: Int) {
        if (gameFinished || aiThinking) return
        if (!engine.isEmpty(index)) return
        if (vsComputer && engine.currentPlayer != GameEngine.X) return

        engine.play(index)
        afterMove()
    }

    /** Вызывается после любого хода: проверяет результат и, если нужно, ход ИИ. */
    private fun afterMove() {
        val result = engine.winner()
        if (result != GameEngine.NO_WINNER) {
            gameFinished = true
            when (result) {
                GameEngine.X -> scoreX++
                GameEngine.O -> scoreO++
            }
        }
        render()
        maybeScheduleAi()
    }

    /** Планирует ход компьютера, если сейчас его очередь и партия не окончена. */
    private fun maybeScheduleAi() {
        if (gameFinished || aiThinking) return
        if (!vsComputer || engine.currentPlayer != GameEngine.O) return

        aiThinking = true
        render()
        handler.postDelayed(aiTurn, AI_DELAY_MS)
    }

    private fun performAiTurn() {
        val move = engine.bestMove(engine.currentPlayer)
        if (move < 0) {
            render()
            return
        }
        engine.play(move)
        afterMove()
    }

    private fun startNewGame() {
        handler.removeCallbacks(aiTurn)
        aiThinking = false
        gameFinished = false
        engine.reset()
        render()
    }

    private fun render() {
        val winLine = engine.winningLine()?.toSet() ?: emptySet()
        cells.forEachIndexed { index, cell ->
            val value = engine.cell(index)
            cell.setImageDrawable(
                when (value) {
                    GameEngine.X -> AppCompatResources.getDrawable(this, R.drawable.mark_x)
                    GameEngine.O -> AppCompatResources.getDrawable(this, R.drawable.mark_o)
                    else -> null
                }
            )
            val background = if (index in winLine) R.color.cell_win else R.color.cell_background
            cell.backgroundTintList = ColorStateList.valueOf(getColor(background))
            cell.contentDescription = getString(
                when (value) {
                    GameEngine.X -> R.string.cell_x
                    GameEngine.O -> R.string.cell_o
                    else -> R.string.cell_empty
                },
                index + 1
            )
        }
        binding.textStatus.text = statusText()
        renderScore()
    }

    private fun renderScore() {
        binding.textScore.text = getString(R.string.score_format, scoreX, scoreO)
    }

    private fun statusText(): String = when {
        gameFinished -> when (engine.winner()) {
            GameEngine.X -> getString(R.string.status_x_win)
            GameEngine.O -> getString(R.string.status_o_win)
            else -> getString(R.string.status_draw)
        }
        aiThinking -> getString(R.string.status_thinking)
        engine.currentPlayer == GameEngine.X -> getString(R.string.status_x_turn)
        else -> getString(R.string.status_o_turn)
    }

    private fun restoreState(state: Bundle?) {
        if (state == null) {
            binding.switchMode.isChecked = vsComputer
            return
        }
        val savedBoard = state.getIntArray(KEY_BOARD)
        if (savedBoard != null) {
            engine.restore(savedBoard, state.getInt(KEY_PLAYER, GameEngine.X))
        }
        scoreX = state.getInt(KEY_SCORE_X, 0)
        scoreO = state.getInt(KEY_SCORE_O, 0)
        vsComputer = state.getBoolean(KEY_VS_COMPUTER, true)
        gameFinished = state.getBoolean(KEY_FINISHED, false)
        binding.switchMode.isChecked = vsComputer
    }

    override fun onSaveInstanceState(outState: Bundle) {
        // Незавершённый ход компьютера доигрываем сразу, чтобы не потерять его при пересоздании.
        if (aiThinking) {
            handler.removeCallbacks(aiTurn)
            aiThinking = false
            performAiTurn()
        }
        super.onSaveInstanceState(outState)
        outState.putIntArray(KEY_BOARD, engine.snapshot())
        outState.putInt(KEY_PLAYER, engine.currentPlayer)
        outState.putInt(KEY_SCORE_X, scoreX)
        outState.putInt(KEY_SCORE_O, scoreO)
        outState.putBoolean(KEY_VS_COMPUTER, vsComputer)
        outState.putBoolean(KEY_FINISHED, gameFinished)
    }

    override fun onDestroy() {
        handler.removeCallbacksAndMessages(null)
        super.onDestroy()
    }

    private companion object {
        const val KEY_BOARD = "board"
        const val KEY_PLAYER = "player"
        const val KEY_SCORE_X = "score_x"
        const val KEY_SCORE_O = "score_o"
        const val KEY_VS_COMPUTER = "vs_computer"
        const val KEY_FINISHED = "finished"

        const val AI_DELAY_MS = 350L
    }
}
