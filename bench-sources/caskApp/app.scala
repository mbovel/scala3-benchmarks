package app

import scalatags.Text.all._

object MinimalApplication extends cask.MainRoutes {
  var messages = Vector(
    ("alice", "Hello World!"),
    ("bob", "I am cow, hear me moo"),
    ("charlie", "I weigh twice as you"),
  )

  var openConnections = Set.empty[cask.WsChannelActor]

  def messageList() = {
    frag(
      for ((name, msg) <- messages)
        yield p(b(name), " ", msg),
    )
  }

  @cask.postJson("/")
  def postHello(name: String, msg: String) = {
    if (name == "") ujson.Obj("success" -> false, "txt" -> "Name cannot be empty")
    else if (name.length >= 10) ujson.Obj("success" -> false, "txt" -> "Name cannot be longer than 10 characters")
    else if (msg == "") ujson.Obj("success" -> false, "txt" -> "Message cannot be empty")
    else if (msg.length >= 160) ujson.Obj("success" -> false, "txt" -> "Message cannot be longer than 160 characters")
    else {
      messages = messages :+ (name -> msg)
      val notification = cask.Ws.Text(
        ujson.Obj("index" -> messages.length, "txt" -> messageList().render).render(),
      )
      for (conn <- openConnections) conn.send(notification)
      openConnections = Set.empty
      ujson.Obj("success" -> true, "txt" -> messageList().render)
    }
  }

  @cask.get("/")
  def hello() = {
    html(
      head(
        link(
          rel := "stylesheet",
          href := "https://stackpath.bootstrapcdn.com/bootstrap/4.3.1/css/bootstrap.min.css",
        ),
        script(raw("""
          function submitForm(){
            fetch(
              "/",
              {
                method: "POST",
                body: JSON.stringify({name: nameInput.value, msg: msgInput.value})
              }
            ).then(response => response.json())
             .then(json => {
              if (json.success) {
                messageList.innerHTML = json.txt
                msgInput.value = ""
                errorDiv.innerText = ""
              } else {
                errorDiv.innerText = json.txt
              }
            })
          }

          var socket = new WebSocket("ws://" + location.host + "/subscribe");
          socket.onopen = function(ev){ socket.send("0") }
          socket.onmessage = function(ev){
            var json = JSON.parse(ev.data)
            messageList.innerHTML = json.txt
            socket.send("" + json.index)
          }
        """)),
      ),
      body(
        div(cls := "container")(
          h1("Scala Chat!"),
          hr,
          div(id := "messageList")(
            messageList(),
          ),
          hr,
          div(id := "errorDiv", color.red),
          form(onsubmit := "submitForm(); return false")(
            input(
              `type` := "text",
              id := "nameInput",
              placeholder := "User name",
              width := "20%",
            ),
            input(
              `type` := "text",
              id := "msgInput",
              placeholder := "Please write a message!",
              width := "60%",
            ),
            input(`type` := "submit", width := "20%"),
          ),
        ),
      ),
    ).render
  }

  @cask.websocket("/subscribe")
  def subscribe() = {
    cask.WsHandler { connection =>
      cask.WsActor {
        case cask.Ws.Text(msg) =>
          if (msg.toInt < messages.length) {
            connection.send(
              cask.Ws.Text(
                ujson.Obj("index" -> messages.length, "txt" -> messageList().render).render(),
              ),
            )
          } else {
            openConnections += connection
          }
        case cask.Ws.Close(_, _) => openConnections -= connection
      }
    }
  }

  initialize()
}
