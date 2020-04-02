# Sample Client

This is a sample client written in Kotlin. It is a good place to start.

The code to be edited is in `src/main/kotlin/app/AI.kt`. It used classes from `src/main/kotlin/app/Board.kt`. It is worth looking at `Board.kt`, as it contains numerous helper operations for working with m, n, k boards that may be of use.

## Running
Running this command:

`./gradlew run --args="http://localhost:8080 secret0 secret1"`

will start two instances of the client, assuming the server is on `localhost:8080` and the client's keys are `secret0` and `secret1`.