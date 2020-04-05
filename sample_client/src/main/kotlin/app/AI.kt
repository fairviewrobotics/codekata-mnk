package app

/**
 * YOU SHOULD EDIT THIS FILE
 */

class AI {
    /**
     * A name to be displayed for your algorithm
     * Keep it fairly short (~20 chars or less)
     */
    val name = "Improved Sample AI Template"

    /**
     * Provide a move for the given board
     * The function should return the move as Pair<Int, Int>,
     * which is the x and y position of where to place the piece
     */
    fun solveBoard(board: Board): Pair<Int, Int> {
        // YOU CAN DELETE OR IMPROVE ALL OF THIS CODE
        // get legal moves we can make on the board
        val moves = board.getLegalMoves(BoardPlayer.US)

        // check for moves that let us win
        for(m in moves) {
            // construct a new board with the move played
            val newBoard = board.withMove(m, BoardPlayer.US)
            // if the move lets us win, play it
            if(newBoard.hasWon(BoardPlayer.US)) return m
        }
        // block moves that let the opponent win
        for(m in moves) {
            // construct a new board with the move played
            val newBoard = board.withMove(m, BoardPlayer.THEM)
            if(newBoard.hasWon(BoardPlayer.THEM)) return m
        }

        for(m in moves) {
            val newBoard = board.withMove(m, BoardPlayer.US)
            var numWins = 0
            val newM = newBoard.getLegalMoves(BoardPlayer.US)
            for(n in newM) {
                if(newBoard.withMove(n, BoardPlayer.US).hasWon(BoardPlayer.US)) numWins++
            }
            if(numWins >= 2) return m
        }

        for(m in moves) {
            val newBoard = board.withMove(m, BoardPlayer.THEM)
            var numWins = 0
            val newM = newBoard.getLegalMoves(BoardPlayer.THEM)
            for(n in newM) {
                if(newBoard.withMove(n, BoardPlayer.THEM).hasWon(BoardPlayer.THEM)) numWins++
            }
            if(numWins >= 2) return m
        }

        // otherwise, pick a random one
        return moves.random()
    }
}