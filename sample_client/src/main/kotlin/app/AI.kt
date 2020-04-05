package app

/**
 * YOU SHOULD EDIT THIS FILE
 */

class AI {
    /**
     * A name to be displayed for your algorithm
     * Keep it fairly short (~20 chars or less)
     */
    val name = "Sample AI Template"

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

        // otherwise, pick a random one
        return moves.random()
    }
}