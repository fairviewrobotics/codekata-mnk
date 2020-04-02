let canvas, render, width, height;

let intervalID;

/* add rounded rect method to canvas */
CanvasRenderingContext2D.prototype.roundRect = function (x, y, w, h, r) {
  if (w < 2 * r) r = w / 2;
  if (h < 2 * r) r = h / 2;
  this.beginPath();
  this.moveTo(x+r, y);
  this.arcTo(x+w, y,   x+w, y+h, r);
  this.arcTo(x+w, y+h, x,   y+h, r);
  this.arcTo(x,   y+h, x,   y,   r);
  this.arcTo(x,   y,   x+w, y,   r);
  this.closePath();
  return this;
}

function pad(n, width, z) {
  z = z || '0';
  n = n + '';
  return n.length >= width ? n : new Array(width - n.length + 1).join(z) + n;
}

/* setup a full page canvas */
window.onload = () => {
    canvas = document.getElementById("screen");
    render = canvas.getContext("2d");

    const resize = () => {
        width = window.innerWidth;
        height = window.innerHeight;
        canvas.width = width;
        canvas.height = height;
    };

    resize();
    window.onresize = resize;

    document.addEventListener('keydown', function(event) {
        if(event.keyCode == 73) {
            window.clearInterval(intervalID);
            result = parseInt(prompt("Enter refresh period (ms)"), 10)
            console.log(result)
            intervalID = window.setInterval(main, result)
        }
    }, false);
};

/* map colors to player index */
function getColorFromPlayerID(index) {
    /* TODO: pick nice colors */
    return ["CornflowerBlue", "Crimson", "DarkOrange", "GoldenRod", "Olive", "SaddleBrown"][index];
}

/* draw a players bar at top */
function drawPlayers(players, x, y, w, h) {
    /* clear drawing area */
    render.fillStyle = "white";
    render.fillRect(x, y, w, h);
    /* divide up for num players */
    const areaEach = w / players.length;
    let loc = 0;
    for(i in players) {
        const p = players[i];
        render.font = "15px monospace";
        render.textAlign = "center";
        render.fillStyle = getColorFromPlayerID(i);
        render.roundRect(loc + 5, y + 5, areaEach - 10, 45, 5).fill();
        render.fillStyle = "white";
        render.fillText(p.name, loc + areaEach/2, y + 22, areaEach - 20);
        render.font = "16px monospace";
        render.fillStyle = "white";
        render.fillText(p.wins + "-" + p.losses + "-" + p.ties + " ("  + p.score.toFixed(2) + ")", loc + areaEach/2, y + 43, areaEach - 20);
        render.textAlign = "start";
        loc += areaEach;
    }
}

/* draw out match schedule sidebar */
function drawMatches(matches, x, y, w, h) {
    /* clear drawing area */
    render.font = "15px monospace";
    render.fillStyle = "white";
    render.fillRect(x, y, w, h);

    const matchHeight = 20;

    render.fillStyle = "black";
    render.fillText("Match", x + 10, y + 20);
    render.fillText("Result", x + 130, y + 20);

    let loc = y + matchHeight;
    for(i in matches) {
        const m = matches[i];
        render.fillStyle = "black";
        render.fillText(pad(i.toString(), 3), x + 10, loc + 23);
        render.fillText("vs", x + 75, loc + 23);

        render.fillStyle = getColorFromPlayerID(m.player1);
        render.roundRect(x + 50, loc + 14, 20, 10, 3).fill();

        render.fillStyle = getColorFromPlayerID(m.player2);
        render.roundRect(x + 100, loc + 14, 20, 10, 3).fill();

        if(m.finished && m.winner != null) {
            if(m.winner == "tie") {
                render.fillStyle = "black";
                render.fillText("tie", x + 145, loc + 23);
            } else {
                render.fillStyle = getColorFromPlayerID(m.winner)
                render.roundRect(x + 150, loc + 14, 20, 10, 3).fill();
            }
        }

        loc += matchHeight;
    }

}

/* draw the body of a single board (no notes around it) */
function drawBoardCore(board, x, y, w, h) {
    /* clear drawing area */
    render.fillStyle = "white";
    render.fillRect(x, y, w, h);

    const m = board.length;
    const n = board[0].length;

    for(xp in board) {
        for(yp in board[xp]) {
            const cell = board[xp][yp];

            render.strokeRect(x + (w/m) * xp, y + (h/n) * yp, (w/m), (h/n));
            if(cell == -1) continue;
            render.fillStyle = getColorFromPlayerID(cell);

            render.beginPath();
            render.arc(x + (w/m) * (xp) + (0.5 * (w/m)), y + (h/n) * yp + (0.5 * (h/n)), Math.min((w/m) * 0.4, (h/n) * 0.4), 0, 2 * Math.PI);

            render.closePath();
            render.fill();
        }
    }
}

/* figure out which player is being waited on in a board */
function nextPlayer(board, matches) {
    if(board.matchID == null) return -1;
    const p1 = matches[board.matchID].player1;
    const p2 = matches[board.matchID].player2;

    let count1 = 0;
    let count2 = 0;
    let isFull = true;

    for(xp in board.board) {
        for(yp in board.board[xp]) {
            const cell = board.board[xp][yp];
            if(cell == p1) count1++;
            else if(cell == p2) count2++;
            else isFull = false;
        }
    }
    if(isFull) return -1;
    if(count1 <= count2) return p1;
    if(count1 > count2)  return p2;
}

/* draw a board with its associated match information */
function drawBoard(board, matches, loc) {
    const x = loc[0];
    const y = loc[1];
    const w = loc[2];
    const h = loc[3];

    drawBoardCore(board.board, x + 30, y + 30, w - 60, h - 60);

    render.font = "15px monospace";
    render.fillStyle = "black";
    /* put match number and information at top of graph */
    if(board.matchID == null) {
        render.fillText("No Active Match", x + w/2 - 65, y + 21);
    } else {
        render.fillText(pad(board.matchID, 3), x + 30, y + 21);
        render.textAlign = "end";
        render.fillText("(" + board.m + "," + board.n + "," + board.k + ")", x + w - 30, y + 21);
        render.textAlign = "start";
        render.fillText("vs", x + w/2 - 10, y + 21);
        const match = matches[board.matchID];

        render.fillStyle = getColorFromPlayerID(match.player1);
        render.roundRect(x + w/2 - 60, y + 8, 40, 14, 3).fill();
        render.fillStyle = getColorFromPlayerID(match.player2);
        render.roundRect(x + w/2 + 20, y + 8, 40, 14, 3).fill();

        const next = nextPlayer(board, matches);
        if(next != -1 && match.finished == false) {
            render.fillStyle = "black";
            render.fillText("Waiting for:", x + w - 200, y + h - 9);
            render.fillStyle = getColorFromPlayerID(next);
            render.roundRect(x + w - 75, y + h - 22, 40, 14, 3).fill();
        }
        if(match.finished) {
            render.fillStyle = "black";
            render.roundRect(x + 30, y + w - 25, w - 60, 20, 5).fill();
            render.fillStyle = "white";
            if(match.winner == "tie") {
                render.textAlign = "center";
                render.fillText("tie", x + w/2, y + w - 10);
                render.textAlign = "start";
            } else {
                render.fillText("won", x + w/2 + 10, y + w - 10);
                render.fillStyle = getColorFromPlayerID(match.winner);
                render.roundRect(x + w/2 - 40, y + w - 22, 40, 14, 3).fill();
            }
        }
    }
}

/* take a rectangular space and return the square area to draw in */
function getSquareArea(x, y, w, h) {
    const minD = Math.min(w, h);
    const cx = x + w/2;
    const cy = y + h/2;

    return [cx - minD/2, cy - minD/2, minD, minD];
}

/* divide an area into a grid with given dimensions, and draw each in the square in those cells */
function drawBoardsGrid(boards, matches, x, y, w, h, rows, cols) {
    let i = 0
    for(let yp = 0; yp < rows; yp++) {
        for(let xp = 0; xp < cols; xp++) {
            const bx = x + xp * (w/cols);
            const by = y + yp * (h/rows);
            loc = getSquareArea(bx, by, w/cols, h/rows);
            if(i < boards.length) drawBoard(boards[i], matches, loc)

            i++
        }
    }
}

/* draw all boards in the appropriate layout (grid) */
function drawBoards(boards, matches, x, y, w, h) {
    render.fillStyle = "white";
    render.fillRect(x, y, w, h);

    const numBoards = boards.length;
    if(numBoards == 1) {
        drawBoardsGrid(boards, matches, x, y, w, h, 1, 1);
    } else if(numBoards == 2) {
        if(w >= h) drawBoardsGrid(boards, matches, x, y, w, h, 1, 2);
        else drawBoardsGrid(boards, matches, x, y, w, h, 2, 1);
    } else if(numBoards == 3) {
        if(w >= h*1.5) drawBoardsGrid(boards, matches, x, y, w, h, 1, 3);
        else if(h >= w*1.5) drawBoardsGrid(boards, matches, x, y, w, h, 3, 1);
        else drawBoardsGrid(boards, matches, x, y, w, h, 2, 2);
    } else if(numBoards == 4) {
        drawBoardsGrid(boards, matches, x, y, w, h, 2, 2);
    } else {
        /* draw a grid as close to a square as possible */
        const sqrt = Math.sqrt(numBoards)
        if(Number.isInteger(sqrt)) drawBoardsGrid(boards, matches, x, y, w, h, sqrt, sqrt);
        else {
            let dim1 = Math.floor(sqrt);
            const dim2 = Math.ceil(sqrt);
            if(dim1 * dim2 < numBoards) dim1++;
            if(w >= h) drawBoardsGrid(boards, matches, x, y, w, h, dim1, dim2);
            else drawBoardsGrid(boards, matches, x, y, w, h, dim2, dim1);
        }
    }
}

/* draws out the dashboard */
async function main() {
    const players = await JSON.parse(await (await fetch('/api/observe/players')).text());
    drawPlayers(players, 0, 0, width, 55);
    const matches = await JSON.parse(await (await fetch('/api/observe/matches')).text());
    drawMatches(matches, width - 200, 55, 200, height - 55);
    const boards = await JSON.parse(await (await fetch('/api/observe/boards')).text());

    drawBoards(boards, matches, 0, 55, width - 200, height - 55);
};

intervalID = window.setInterval(main, 1000);
