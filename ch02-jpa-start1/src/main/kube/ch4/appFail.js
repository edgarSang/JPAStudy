const http = require('http');
const os = require('os');
let i = 0;

console.log("Kubia server starting…");

var handler = function(request, response) {
  console.log("Received request from" + request.connection.remoteAddress +" i:" + i);
  if(i < 5) {
    response.writeHead(200);
    response.end("You've hit " + os.hostname() + " i:"+i+ "\n");
    i++;
  } else {
    response.writeHead(400);
    response.end("You've ERROR!!! Code:400 and" + os.hostname() + "\n");
  }
}

var www = http.createServer(handler);
www.listen(28080);
