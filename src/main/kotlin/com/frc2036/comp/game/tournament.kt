package com.frc2036.comp.game

// A play in a tournament
// key is the secret key used to authenticate api access
// won-lost-tied is their match record, score = won - lost / games
data class Player(val key: String, var name: String, var wins: Int, var losses: Int, var ties: Int, var score: Double)

// A match in a tournament, mapping tournament players to their role as BoardPlayers
// winner is null if match has not been played
data class Match(val player1: Player, val player2: Player, var winner: Player?)

data class TournamentBoard(val board: MNKBoard, var activeMatch: Match?)

// a round robin tournament (a set of players and games to be run )
class RRTournament(val playerKeys: List<String>, val numMatchesAtOnce: Int, val makeBoard: () -> MNKBoard) {
    val players = playerKeys.map {k -> Player(k, "AI #${playerKeys.indexOf(k)}", 0, 0, 0, 0.0) }
    val schedule = makeSchedule()

    val boards = List(numMatchesAtOnce) { TournamentBoard(makeBoard(), null) }

    init {
        boards[0].activeMatch = schedule[1]
        boards[0].board.doMove(1, 1, BoardPlayer.PLAYER_ONE)
        boards[0].board.doMove(0, 1, BoardPlayer.PLAYER_TWO)
        boards[0].board.doMove(3, 3, BoardPlayer.PLAYER_ONE)
        boards[0].board.doMove(4, 1, BoardPlayer.PLAYER_TWO)
    }

    // get player id (index)
    fun getPlayerID(p: Player) = players.indexOf(p)

    // make a schedule such that each player faces the other twice (once as player1, once as player2)
    fun makeSchedule(): List<Match> {
        val matches = mutableListOf<Match>()
        for(player1 in players) {
            for(player2 in players) {
                if(player1 == player2) continue
                matches.add(Match(player1, player2, null))
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
}