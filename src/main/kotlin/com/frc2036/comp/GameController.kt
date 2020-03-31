package com.frc2036.comp

import com.frc2036.comp.game.BoardPlayer
import com.frc2036.comp.game.MNKBoard
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestMethod
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.bind.annotation.RequestParam

import com.frc2036.comp.game.RRTournament
import com.frc2036.comp.game.TournamentBoard

// Controller to manage api calls to run the tournament
@RestController
@RequestMapping(value=["/api"])
class GameController {

    val tournament = RRTournament(listOf("secret1", "secret2", "secret3", "secret4", "secret5", "secret6"), 2, { MNKBoard(5, 5, 4) })

    /**
     * Set an Player's name
     */
    @RequestMapping(value=["/set_name"], method=[RequestMethod.POST], produces=["application/json"])
    fun setName(@RequestParam key: String, @RequestParam name: String): String {
        return if(tournament.setName(key, name)) "{ \"error\": false }" else "{ \"error\": \"Invalid Key\" }"
    }

    /**
     * Return the match schedule as a json object:
     * [{player1: Int, player2: Int, finished: Boolean, winner: Int}, ...]
     */
    @RequestMapping(value=["/observe/matches"], method=[RequestMethod.GET], produces=["application/json"])
    fun matches(): String {
        return "${tournament.schedule.map {m -> "{" +
            "\"player1\": ${tournament.getPlayerID(m.player1)}, " +
            "\"player2\": ${tournament.getPlayerID(m.player2)}, " +
            "\"finished\": ${m.winner != null}, " +
            "\"winner\": ${if(m.winner != null) tournament.getPlayerID(m.winner!!).toString() else "null"}" +
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
     * [{matchID: null|Int, board: [[Int, Int, ...], ...]}]
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
            "\"board\": ${b.board.contents.mapIndexed {row, _ -> b.board.contents[row].mapIndexed {col, _ -> playerToNum(b, b.board.contents[row][col]) }}}" +
            "}"}}"
    }
}