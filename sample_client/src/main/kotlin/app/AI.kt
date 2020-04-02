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
        // get legal moves we can make on the board
        val moves = board.getLegalMoves(BoardPlayer.US)

        // check for moves that let us win
        for(m in moves) {
            val newBoard = board.withMove(m, BoardPlayer.US)
            if(newBoard.hasWon(BoardPlayer.US)) return m
        }

        // check for a move where the opponent would win
        for(m in moves) {
            val newBoard = board.withMove(m, BoardPlayer.THEM)
            if(newBoard.hasWon(BoardPlayer.THEM)) return m
        }

        // if we can fork, play the fork
        for(m in moves) {
            val newBoard = board.withMove(m, BoardPlayer.US)
            /* check if we have two or more wins */
            val secMoves = newBoard.getLegalMoves(BoardPlayer.US)
            val numWins = secMoves.count {s -> newBoard.withMove(s, BoardPlayer.US).hasWon(BoardPlayer.US)}
            if(numWins >= 2) {
                println("Forked!")
                return m
            }
        }

        // if we can block a fork, block it
        for(m in moves) {
            val newBoard = board.withMove(m, BoardPlayer.THEM)
            /* check if they have two or more wins */
            val secMoves = newBoard.getLegalMoves(BoardPlayer.THEM)
            val numWins = secMoves.count {s -> newBoard.withMove(s, BoardPlayer.THEM).hasWon(BoardPlayer.THEM)}
            if(numWins >= 2) {
                println("Blocked Fork!")
                return m
            }
        }

        // pick a random one
        return moves.random()
    }

    /*fun solveBoard(board: Board): Pair<Int, Int> {
        val moves = board.getLegalMoves(BoardPlayer.US)

        return moves.random()
    }*/
}