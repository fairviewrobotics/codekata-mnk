package com.frc2036.comp

import com.frc2036.comp.game.BoardPlayer
import com.frc2036.comp.game.MNKBoard
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestMethod
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.bind.annotation.RequestParam

import com.frc2036.comp.game.RRTournament
import com.frc2036.comp.game.TournamentBoard
import kotlin.random.Random

// Controller to manage api calls to run the tournament
@RestController
@RequestMapping(value=["/api"])
class GameController {
    val tournament = RRTournament(listOf("secret0", "secret1"), 1, {
        val m = Random.nextInt(3,15)
        val n = Random.nextInt(3, 15)
        val k = minOf(Random.nextInt(3, 5), m, n)
        MNKBoard(m, n, k)
    }, 8)

    /**
     * Get the current board for a player to solve as a json object:
     * null|
     * {m: Int, n: Int, k: Int, board: [[Int, Int, ...], ...]}
     * Each cell in the board is an integer. A -1 indicates the cell is open, a 0 indicates it has the player's piece, a 1 indicates it has the opponents piece
     */
    @RequestMapping(value=["/board"], method=[RequestMethod.GET], produces=["application/json"])
    @Synchronized
    fun getBoardToPlay(@RequestParam key: String): String {
        val player = tournament.players.find { p -> p.key == key} ?: return "null"
        val board = tournament.getBoardToRun(key) ?: return "null"
        return "{\"m\": ${board.board.m}, \"n\": ${board.board.n}, \"k\": ${board.board.k}," +
            "\"board\": ${board.board.contents.mapIndexed {x, _ -> board.board.contents[x].mapIndexed {y, _ ->
            if(board.board.contents[x][y] == BoardPlayer.NONE) -1 else (
                if((board.board.contents[x][y] == BoardPlayer.PLAYER_ONE && board.activeMatch?.player1 == player)
                    || (board.board.contents[x][y] == BoardPlayer.PLAYER_TWO && board.activeMatch?.player2 == player)) 0 else 1    
            )
        }}}}"
    }

    /**
     * Make a move on the player's current board
     */
    @RequestMapping(value=["/move"], method=[RequestMethod.POST], produces=["application/json"])
    @Synchronized
    fun makeMove(@RequestParam key: String, x: Int, y: Int): String {
        val player = tournament.players.find { p -> p.key == key} ?: return "{ \"error\": \"Invalid Key\" }"
        val board = tournament.getBoardToRun(key) ?: return "{ \"error\": \"Player doesn't have active board\" }"
        // make sure player is playing on board
        val boardPlayer = (if(board.activeMatch?.player1 == player) BoardPlayer.PLAYER_ONE else
            (if(board.activeMatch?.player2 == player) BoardPlayer.PLAYER_TWO else BoardPlayer.NONE))
        if(boardPlayer == BoardPlayer.NONE) return "{ \"error\": \"Player isn't on board\" }"
        if(board.board.getNextPlayer() != boardPlayer) return "{ \"error\": \"Player moved out of turn\" }"
        // TODO: if move is illegal, make a legal move so next player can move
        if(!board.board.checkLegal(x, y, boardPlayer)) return "{ \"error\": \"illegal move\" }"
        board.board.doMove(x, y, boardPlayer)

        // update for wins, etc
        tournament.manageBoards()

        return "{ \"error\": null }"
    }

    /**
     * Set an Player's name (not displayed by frontend)
     */
    @Synchronized
    @RequestMapping(value=["/set_name"], method=[RequestMethod.POST], produces=["application/json"])
    fun setName(@RequestParam key: String, @RequestParam name: String): String {
        return if(tournament.setName(key, name)) "{ \"error\": null }" else "{ \"error\": \"Invalid Key\" }"
    }

    /**
     * Return the match schedule as a json object:
     * [{player1: Int, player2: Int, finished: Boolean, winner: Int|"tie"|null}, ...]
     *
     * Note: the winner may be "tie"
     */
    @RequestMapping(value=["/observe/matches"], method=[RequestMethod.GET], produces=["application/json"])
    fun matches(): String {
        return "${tournament.schedule.map {m -> "{" +
            "\"player1\": ${tournament.getPlayerID(m.player1)}, " +
            "\"player2\": ${tournament.getPlayerID(m.player2)}, " +
            "\"finished\": ${m.winner != null || m.tie}, " +
            "\"winner\": ${if(m.tie) "\"tie\"" else (if(m.winner != null) tournament.getPlayerID(m.winner!!).toString() else "null")}" +
            "}"}}"
    }

    /**
     * Return information for each player as a json object:
     * [{name: String, wins: Int, losses: Int, ties: Int, score: Int}, ...]
     */
    @RequestMapping(value=["/observe/players"], method=[RequestMethod.GET], produces=["application/json"])
    fun players(): String {
        return "${tournament.players.map {p -> "{" +
            "\"name\": \"${p.name}\", " +
            "\"wins\": ${p.wins}, \"losses\": ${p.losses}, \"ties\": ${p.ties}," +
            "\"score\": ${p.score}" +
            "}"}}"
    }

    /**
     * Return the state of all of the tournament boards
     * [{matchID: null|Int, m: Int, n: Int, k: Int, board: [[Int, Int, ...], ...]}]
     * Each board is an array of arrays. Indexed [x][y]
     * Each entry in the board is -1 if no player has gone there, or an id corresponding to the player who has
     */
    @RequestMapping(value=["/observe/boards"], method=[RequestMethod.GET], produces=["application/json"])
    fun boards(): String {
        fun playerToNum(b: TournamentBoard, player: BoardPlayer): Int {
            if (player == BoardPlayer.NONE || b.activeMatch == null) return -1
            if (player == BoardPlayer.PLAYER_ONE) return tournament.getPlayerID(b.activeMatch!!.player1)
            if (player == BoardPlayer.PLAYER_TWO) return tournament.getPlayerID(b.activeMatch!!.player2)
            return -1
        }
        return "${tournament.boards.map {b -> "{" +
            "\"matchID\": ${if(b.activeMatch == null) "null" else tournament.schedule.indexOf(b.activeMatch!!).toString()}, " +
            "\"m\": ${b.board.m}, \"n\": ${b.board.n}, \"k\": ${b.board.k}, " +
            "\"board\": ${b.board.contents.mapIndexed {x, _ -> b.board.contents[x].mapIndexed {y, _ -> playerToNum(b, b.board.contents[x][y]) }}}" +
            "}"}}"
    }
}