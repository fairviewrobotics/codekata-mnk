package app

/**
 * YOU CAN EDIT THIS FILE
 * Utility functions for representing and operating on the game board
 * You may want to add helper methods to the Board class
 */

// a player on a board
enum class BoardPlayer {
    NONE, US, THEM
}

// a winner of a board
enum class BoardWinner {
    NONE, US, THEM, TIE
}

// An (m,n,k) board - A m x n board where k in a row is a win
class Board(val m: Int, val n: Int, val k: Int) {
    // contents is indexed [x][y]
    val contents = Array(m) { Array(n) { BoardPlayer.NONE } }

    // check if any player has won the board
    // return BoardPlayer.NONE if no one has won, the player otherwise
    fun getWinner(): BoardWinner {
        if(hasWon(BoardPlayer.US)) return BoardWinner.US
        if(hasWon(BoardPlayer.THEM)) return BoardWinner.THEM
        if(isFull()) return BoardWinner.TIE
        return BoardWinner.NONE
    }

    // check if a player has won the board
    fun hasWon(player: BoardPlayer): Boolean {
        /* check columns */
        for(x in 0 until m) {
            var numCont = 0
            for(y in 0 until n) {
                if(contents[x][y] == player) numCont++
                else numCont = 0

                if(numCont >= k) return true
            }
        }
        /* check rows */
        for(y in 0 until n) {
            var numCont = 0
            for(x in 0 until m) {
                if(contents[x][y] == player) numCont++
                else numCont = 0

                if(numCont >= k) return true
            }
        }
        /* check diagonals */
        /* check down and left diagonals */
        for(i in 0 until (m + n - 1)) {
            var numCont = 0
            val j = if(i < m) 0 else i - m + 1
            val z = if(i < n) i + 1 else n
            for(y in j until z) {
                val x = i - y

                if(contents[x][y] == player) numCont++
                else numCont = 0

                if(numCont >= k) return true
            }
        }
        /* check down and right diagonals */
        for(i in 0 until (m + n - 1)) {
            var numCont = 0
            val j = if(i < n) n - i - 1 else 0
            val z = if(i < m) n else m + n - i - 1
            for(y in j until z) {
                val x = i + y - n + 1

                if(contents[x][y] == player) numCont++
                else numCont = 0

                if(numCont >= k) return true
            }
        }

        return false
    }

    // return a new board with the specified move played
    fun withMove(position: Pair<Int, Int>, player: BoardPlayer): Board {
        val (x, y) = position
        if(!checkLegal(position, player)) return this

        val res = Board(m, n, k)
        for(cx in 0 until m) {
            for(cy in 0 until n) {
                res.contents[cx][cy] = contents[cx][cy]
            }
        }

        res.contents[x][y] = player

        return res
    }

    // mutate the board with a specified move
    fun doMove(position: Pair<Int, Int>, player: BoardPlayer) {
        val (x, y) = position
        if(!checkLegal(position, player)) return
        contents[x][y] = player
    }

    // mutate the board and clear it
    fun doClear() {
        for(x in 0 until m) {
            for(y in 0 until n) {
                contents[x][y] = BoardPlayer.NONE
            }
        }
    }

    // check if a move is legal
    fun checkLegal(position: Pair<Int, Int>, player: BoardPlayer): Boolean {
        val (x, y) = position
        if(x >= m || y >= n || x < 0 || y < 0) return false
        if(player != BoardPlayer.US && player != BoardPlayer.THEM) return false
        if(contents[x][y] != BoardPlayer.NONE) return false
        return true
    }

    // return which player has to go
    fun getNextPlayer(): BoardPlayer {
        /* count number of pieces */
        var p1 = 0
        var p2 = 0

        for(x in 0 until m) {
            for(y in 0 until n) {
                if(contents[x][y] == BoardPlayer.US) p1++
                if(contents[x][y] == BoardPlayer.THEM) p2++
            }
        }

        if(p1 <= p2) return BoardPlayer.US
        else return BoardPlayer.THEM
    }

    // check if a board is full (tied)
    fun isFull(): Boolean {
        for(x in 0 until m) {
            for(y in 0 until n) {
                if(contents[x][y] == BoardPlayer.NONE) return false
            }
        }

        return true
    }

    // return a list of legal moves for the player (they are the same regardless of the player) */
    fun getLegalMoves(player: BoardPlayer): List<Pair<Int, Int>> {
        val res = mutableListOf<Pair<Int, Int>>()
        for(x in 0 until m) {
            for(y in 0 until n) {
                if(checkLegal(Pair(x, y), player)) res.add(Pair(x, y))
            }
        }

        return res
    }

    override fun equals(other: Any?): Boolean {
        if(other !is Board) return false
        if(other.m != m || other.n != n || other.k != k) return false
        for(x in 0 until m) {
            for (y in 0 until n) {
                if(other.contents[x][y] != contents[x][y]) return false
            }
        }

        return true
    }
}