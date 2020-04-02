package app

import kotlin.system.exitProcess
import com.github.kittinunf.fuel.httpGet
import com.github.kittinunf.fuel.httpPost
import com.google.gson.Gson
import kotlin.concurrent.thread

/**
 * DON'T EDIT THIS FILE (edit AI.kt instead)
 *
 * This file contains the Runner class, which manages the connection with the server
 * It waits for a board to solve, and passes it to the AI when the server gives it one
 */

class SetNameResponse(val error: String?)
class BoardResponse(val m: Int, val n: Int, val k: Int, val board: Array<Array<Int>>)

class Runner(val ai: AI, val apiUrl: String, val apiKey: String, val refreshInterval: Long) {
    val gson = Gson()
    fun setName() {
        "${apiUrl}/api/set_name".httpPost(
            listOf(
                Pair("key", apiKey),
                Pair("name", ai.name)
            )
        ).response { request, response, result ->
            if(response.statusCode != 200) {
                System.err.println("Error setting name")
            } else {
                val didError = gson.fromJson(response.body().asString("application/json"), SetNameResponse::class.java)
                if(didError.error != null) System.err.println("Error setting name: ${didError.error}")
                else println("Set name successfully")
            }
        }
    }

    fun checkBoard(): Board? {
        println("Checking for board to solve")
        val (request, response, result) = "${apiUrl}/api/board".httpGet(
            listOf(
                Pair("key", apiKey)
            )
        ).responseString()

        if(response.statusCode != 200) {
            System.err.println("Error loading board")
            return null
        }
        val boardJson = gson.fromJson(response.body().asString("application/json"), BoardResponse::class.java) ?: return null
        val board = Board(boardJson.m, boardJson.n, boardJson.k)
        for(x in 0 until boardJson.m) {
            for(y in 0 until boardJson.n) {
                board.contents[x][y] = if(boardJson.board[x][y] == -1) BoardPlayer.NONE else (
                    if(boardJson.board[x][y] == 0) BoardPlayer.US else (
                        if(boardJson.board[x][y] == 1) BoardPlayer.THEM else BoardPlayer.NONE))
            }
        }

        return board
    }

    fun makeMove(move: Pair<Int, Int>) {
        val (x, y) = move
        "${apiUrl}/api/move".httpPost(
            listOf(
                Pair("key", apiKey),
                Pair("x", x),
                Pair("y", y)
            )
        ).response { request, response, result ->
            if(response.statusCode != 200) {
                System.err.println("Error making move")
            } else {
                val didError = gson.fromJson(response.body().asString("application/json"), SetNameResponse::class.java)
                if(didError.error != null) System.err.println("Error making move: ${didError.error}")
                else println("Success made move")
            }
        }
    }

    fun mainLoop() {
        while(true) {
            val board = checkBoard()
            if(board != null) {
                println("Solving Board...")
                val move = ai.solveBoard(board)
                // check that board is the same
                if(checkBoard() != board) System.err.println("Board to solve changed during solving")
                println("Making move: ${move.first}, ${move.second}")
                makeMove(move)
                // give server time to make move internally and update /api/board
                Thread.sleep(refreshInterval)
            }
            Thread.sleep(refreshInterval)
        }
    }

    companion object {

        @JvmStatic
        fun main(args: Array<String>) {
            if(args.size < 2) {
                println("Expected API url as first command line argument and API key as second")
                exitProcess(1)
            }

            for(i in 1 until args.size) {
                thread {
                    val run = Runner(AI(), args[0], args[i], 1000)
                    println("Starting. API URL: ${args[0]}, API KEY: ${args[1]}")
                    run.setName()
                    run.mainLoop()
                }
            }
        }
    }
}