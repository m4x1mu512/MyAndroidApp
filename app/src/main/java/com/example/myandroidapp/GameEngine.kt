package com.example.myandroidapp

/**
 * Логика игры «Крестики-нолики» на поле 3×3.
 *
 * Движок ничего не знает про Android: поле хранится в [IntArray],
 * а ход ИИ считается алгоритмом «минимакс», поэтому обыграть его нельзя
 * (лучший исход для человека — ничья).
 */
class GameEngine {

    private val board = IntArray(CELLS) { EMPTY }

    /** Чей сейчас ход: [X] или [O]. */
    var currentPlayer: Int = X
        private set

    /** Очищает поле и отдаёт первый ход крестикам. */
    fun reset() {
        board.fill(EMPTY)
        currentPlayer = X
    }

    fun cell(index: Int): Int = board[index]

    fun isEmpty(index: Int): Boolean = board[index] == EMPTY

    /** Ставит метку текущего игрока. Возвращает false, если ход невозможен. */
    fun play(index: Int): Boolean {
        if (index !in board.indices || board[index] != EMPTY) return false
        board[index] = currentPlayer
        currentPlayer = currentPlayer.opponent()
        return true
    }

    /** Результат партии: [X], [O], [DRAW] или [NO_WINNER], если игра идёт. */
    fun winner(): Int {
        for (line in WIN_LINES) {
            val first = board[line[0]]
            if (first != EMPTY && first == board[line[1]] && first == board[line[2]]) return first
        }
        return if (board.none { it == EMPTY }) DRAW else NO_WINNER
    }

    /** Индексы клеток победной линии или null, если победы нет. */
    fun winningLine(): IntArray? {
        for (line in WIN_LINES) {
            val first = board[line[0]]
            if (first != EMPTY && first == board[line[1]] && first == board[line[2]]) {
                return intArrayOf(line[0], line[1], line[2])
            }
        }
        return null
    }

    /** Лучший ход для [player] по минимаксу; -1, если свободных клеток нет. */
    fun bestMove(player: Int): Int {
        var bestScore = Int.MIN_VALUE
        var bestIndex = -1
        for (index in board.indices) {
            if (board[index] != EMPTY) continue
            board[index] = player
            val score = minimax(player.opponent(), player, 1)
            board[index] = EMPTY
            if (score > bestScore) {
                bestScore = score
                bestIndex = index
            }
        }
        return bestIndex
    }

    private fun minimax(turn: Int, ai: Int, depth: Int): Int {
        when (winner()) {
            ai -> return SCORE_WIN - depth
            ai.opponent() -> return depth - SCORE_WIN
            DRAW -> return 0
        }
        var best = if (turn == ai) Int.MIN_VALUE else Int.MAX_VALUE
        for (index in board.indices) {
            if (board[index] != EMPTY) continue
            board[index] = turn
            val score = minimax(turn.opponent(), ai, depth + 1)
            board[index] = EMPTY
            best = if (turn == ai) maxOf(best, score) else minOf(best, score)
        }
        return best
    }

    /** Копия поля — для сохранения состояния при повороте экрана. */
    fun snapshot(): IntArray = board.copyOf()

    /** Восстанавливает поле из [snapshot]. */
    fun restore(saved: IntArray, player: Int) {
        require(saved.size == CELLS) { "Ожидалось $CELLS клеток, получено ${saved.size}" }
        saved.copyInto(board)
        currentPlayer = player
    }

    private fun Int.opponent(): Int = if (this == X) O else X

    companion object {
        const val SIZE = 3
        const val CELLS = SIZE * SIZE

        const val EMPTY = 0
        const val X = 1
        const val O = 2

        const val NO_WINNER = 0
        const val DRAW = 3

        private const val SCORE_WIN = 10

        /** Все выигрышные тройки индексов. */
        private val WIN_LINES = arrayOf(
            intArrayOf(0, 1, 2), intArrayOf(3, 4, 5), intArrayOf(6, 7, 8),
            intArrayOf(0, 3, 6), intArrayOf(1, 4, 7), intArrayOf(2, 5, 8),
            intArrayOf(0, 4, 8), intArrayOf(2, 4, 6)
        )
    }
}
