# m,n,k Game Competition

An m,n,k game is a game in which two players alternate placing stones on a m by n board. The player who is the first to place k stones in a row (horizontally, vertically, or diagonally) wins.

Tic-tac-toe, for example, is the (3,3,3) game.

The game server implementation is in `server/`. A sample client is in `sample_client/`.

M and N will always be no more than 15 and no less than 3. K will always be no less than 3 and no more than the minimum of M and N.

## Server
In the server directory, the server is run by `./gradlew bootRun`. By default, it will serve on `localhost:8080`. Note: gradle will report progress on the `bootRun` task to be stuck at 80% while the server is running.

The server implements the rules of the m,n,k game and is responsible for managing matches, running a tournament, and giving clients boards to provide moves for. Clients are responsible for solving (providing a move for) boards given to them by the server.

Go to `localhost:8080` to see a graphical representation of the current games and matches.

### API Calls
Each client requires a key for api calls. By default, they are `secret` followed by the team number (`secret0`, `secret1`, and so on). For a real competition, keys will be distributed.

By default, the server is configured to run a tournament with two clients with keys (`secret0` and `secret1`)

All API calls return valid JSON.

#### API Routes
`GET URL/api/board - params(key: String)`

Get the board the client currently has to solve. If they client does not have a board to solve (not their turn), the response is `null`. Otherwise, the response is of the form:
```json
{
  "m": Int, "n": Int, "k": Int,
  "board": [
    [Int, Int, Int, ...],
    [Int, Int, Int, ...],
    ...
  ]
}
```
The `board` field of the response is an array of length `m`. Each element of `board` is an array of length `n`. Each element of that array is an integer, representing a cell on the board. It is `-1` if the cell is empty, `0` if it is occupied by one of the client's stones, and `1` if it is occupied by one of the enemy's stones

`POST URL/api/move - params(key: String, x: Int, y: Int)`
Place a stone at (x, y). If no error occurred, the response is:
```json
{ "error": null }
```
Otherwise, the response is
```json
{ "error": "message" }
```
If a client makes an illegal move, the server will play for them.

`POST URL/api/set_name - params(key: String, name: String)`
THIS ROUTE IS OPTIONAL

Set a name to be displayed on the graphical interface.