package com.frc2036.comp.game

import kotlin.concurrent.thread

// A play in a tournament
// key is the secret key used to authenticate api access
// won-lost-tied is their match record, score = won - lost / games
data class Player(val key: String, var name: String, var wins: Int, var losses: Int, var ties: Int, var score: Double) {
    fun reCalculateScore() {
        val totalMatch = wins + losses + ties
        score = (wins - losses).toDouble() / totalMatch.toDouble()
    }
}

// A match in a tournament, mapping tournament players to their role as BoardPlayers
// winner is null if match has not been played
data class Match(val player1: Player, val player2: Player, var winner: Player?, var tie: Boolean)

// if isWaitingToClear the match is over, but is in a short waiting period to display the result.
data class TournamentBoard(val board: MNKBoard, var activeMatch: Match?, var isWaitingToClear: Boolean)

// a round robin tournament (a set of players and games to be run )
class RRTournament(val playerKeys: List<String>, val numMatchesAtOnce: Int, val makeBoard: () -> MNKBoard) {
    val players = playerKeys.map {k -> Player(k, "Unnamed #${playerKeys.indexOf(k)}", 0, 0, 0, 0.0) }
    val schedule = makeSchedule()
    var nextMatchIndex = 0

    val boards = List(numMatchesAtOnce) { TournamentBoard(makeBoard(), null, false) }

    init {
        manageBoards()
    }

    // get player id (index)
    fun getPlayerID(p: Player) = players.indexOf(p)

    // make a schedule such that each player faces the other twice (once as player1, once as player2)
    fun makeSchedule(): List<Match> {
        val matches = mutableListOf<Match>()
        for(player1 in players) {
            for(player2 in players) {
                if(player1 == player2) continue
                matches.add(Match(player1, player2, null, false))
            }
        }
        matches.shuffle()

        return matches
    }

    @Synchronized
    fun setName(key: String, name: String): Boolean {
        val player = players.find {p -> p.key == key}
        player?.name = name
        return player != null
    }

    // update and handle matches assigned to boards
    @Synchronized
    fun manageBoards() {
        // check boards for win
        for(b in boards) {
            val win = b.board.getWinner()
            if(b.activeMatch == null || win == BoardWinner.NONE) continue
            if(b.isWaitingToClear) continue
            val winner = if(win == BoardWinner.PLAYER_ONE) b.activeMatch!!.player1 else
                (if(win == BoardWinner.PLAYER_TWO) b.activeMatch!!.player2 else null)
            val losser = if(win == BoardWinner.PLAYER_ONE) b.activeMatch!!.player2 else
                (if(win == BoardWinner.PLAYER_TWO) b.activeMatch!!.player1 else null)
            // set winner in matches
            b.activeMatch!!.winner = winner
            b.activeMatch!!.tie = (win == BoardWinner.TIE)
            // adjust statistics for players
            if(win == BoardWinner.TIE) {
                b.activeMatch!!.player1.ties++
                b.activeMatch!!.player2.ties++
            } else {
                winner!!.wins++
                losser!!.losses++
            }
            b.activeMatch!!.player1.reCalculateScore()
            b.activeMatch!!.player2.reCalculateScore()

            b.isWaitingToClear = true

            thread {
                Thread.sleep(5000)
                b.activeMatch = null
                b.board.doClear()
                b.isWaitingToClear = false
            }
        }

        // Assign empty board matches to run
        for(b in boards) {
            if(b.activeMatch != null || b.isWaitingToClear) continue
            if(nextMatchIndex < schedule.size) b.activeMatch = schedule[nextMatchIndex++]
        }
    }

    // get the board that a certain player has to provide a move for (the first board waiting on them)
    fun getBoardToRun(key: String): TournamentBoard? {
        val player = players.find { p -> p.key == key} ?: return null
        for(b in boards) {
            if(b.activeMatch == null || b.isWaitingToClear) continue
            if(player == b.activeMatch?.player1 && b.board.getNextPlayer() == BoardPlayer.PLAYER_ONE) return b
            if(player == b.activeMatch?.player2 && b.board.getNextPlayer() == BoardPlayer.PLAYER_TWO) return b
        }
        return null
    }
}