function findGetParameter(parameterName) {
    var result = null,
        tmp = [];
    location.search
        .substr(1)
        .split("&")
        .forEach(function (item) {
          tmp = item.split("=");
          if (tmp[0] === parameterName) result = decodeURIComponent(tmp[1]);
        });
    return result;
}

window.onload = function() {
    document.getElementById("start").addEventListener("click", () => {
        fetch("/api/admin/start", {
            method: "post",
            headers:
            {
                "Content-Type": "application/x-www-form-urlencoded"
            },
            body: "key=" + findGetParameter("key") + "&numBoards=" + document.getElementById("numBoards").value + "&numRematches=" + document.getElementById("numRematches").value + "&playerKeys=" + document.getElementById("playerKeys").value
        }).then((response) => {
            console.log(response);
            if(response.status != 200) window.alert("Request Failed");
        });
    });
    document.getElementById("stop").addEventListener("click", () => {
        fetch("/api/admin/stop", {
            method: "post",
            headers:
            {
                "Content-Type": "application/x-www-form-urlencoded"
            },
            body: "key=" + findGetParameter("key")
        }).then((response) => {
            if(response.status != 200) window.alert("Request Failed");
        });
    });
}