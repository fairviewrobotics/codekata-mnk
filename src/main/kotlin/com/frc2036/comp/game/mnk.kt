package com.frc2036.comp.game

// a player on a board
enum class BoardPlayer {
    NONE, PLAYER_ONE, PLAYER_TWO
}

// An (m,n,k) board - A m x n board where k in a row is a win
class MNKBoard(val m: Int, val n: Int, val k: Int) {
    // contents is indexed [x][y]
    val contents = Array(m) { Array(n) { BoardPlayer.NONE } }

    // check if any player has won the board
    // return BoardPlayer.NONE if no one has won, the player otherwise
    fun getWinner(): BoardPlayer {
        if(hasWon(BoardPlayer.PLAYER_ONE)) return BoardPlayer.PLAYER_ONE
        if(hasWon(BoardPlayer.PLAYER_TWO)) return BoardPlayer.PLAYER_TWO
        return BoardPlayer.NONE
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
    fun withMove(x: Int, y: Int, player: BoardPlayer): MNKBoard {
        if(!checkLegal(x, y, player)) return this

        val res = MNKBoard(m, n, k)
        for(cx in 0 until m) {
            for(cy in 0 until n) {
                res.contents[cx][cy] = contents[cx][cy]
            }
        }

        res.contents[x][y] = player

        return res
    }

    // mutate the board with a specified move
    fun doMove(x: Int, y: Int, player: BoardPlayer) {
        if(!checkLegal(x, y, player)) return
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
    fun checkLegal(x: Int, y: Int, player: BoardPlayer): Boolean {
        if(x >= m || y >= n || x < 0 || y < 0) return false
        if(player != BoardPlayer.PLAYER_ONE && player != BoardPlayer.PLAYER_TWO) return false
        if(contents[x][y] != BoardPlayer.NONE) return false
        return true
    }
}