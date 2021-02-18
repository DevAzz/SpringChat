<#macro page>
    <!DOCTYPE html>
    <html lang="en">
    <head>
        <meta charset="UTF-8">
        <title>TheChat</title>
        <link rel="stylesheet" href="/static/style.css"/>
        <!-- Required meta tags -->
        <meta name="viewport" content="width=device-width, initial-scale=1, shrink-to-fit=no">

        <!-- Bootstrap CSS -->
        <link rel="stylesheet" href="https://stackpath.bootstrapcdn.com/bootstrap/4.4.1/css/bootstrap.min.css" integrity="sha384-Vkoo8x4CGsO3+Hhxv8T/Q5PaXtkKtu6ug5TOeNV6gBiFeWPGFN9MuhOf23Q9Ifjh" crossorigin="anonymous">
    </head>
    <body onload="init()">
    <#include "navbar.ftlh">
    <div class="container mt-5">
        <#nested>
    </div>
    <!-- Optional JavaScript -->
    <!-- jQuery first, then Popper.js, then Bootstrap JS -->
    <script src="https://code.jquery.com/jquery-3.4.1.slim.min.js" integrity="sha384-J6qa4849blE2+poT4WnyKhv5vZF5SrPo0iEjwBvKU7imGFAV0wwj1yYfoRSJoZ+n" crossorigin="anonymous"></script>
    <script src="https://cdn.jsdelivr.net/npm/popper.js@1.16.0/dist/umd/popper.min.js" integrity="sha384-Q6E9RHvbIyZFJoft+2mJbHaEWldlvI9IOYy5n3zV9zzTtmI3UksdQRVvoxMfooAo" crossorigin="anonymous"></script>
    <script src="https://stackpath.bootstrapcdn.com/bootstrap/4.4.1/js/bootstrap.min.js" integrity="sha384-wfSDF2E50Y2D1uUdj0O3uMBJnjuUD4Ih7YwaYd1iqfktj0Uod8GCExl3Og8ifwB6" crossorigin="anonymous"></script>
    <script src="js/stomp.js"></script>
    <script src="https://cdn.jsdelivr.net/npm/sockjs-client@1/dist/sockjs.min.js"></script>
    <script type="text/javascript">
        let stompClient = null;

        function init() {
            let socket = new SockJS('/chat');
            stompClient = Stomp.over(socket);

            let onConnect = function() {
                stompClient.subscribe("/topic/messages", function(message) {
                    showMessage(JSON.parse(message.body));
                });
            };
            stompClient.connect({}, onConnect);
        }

        function showMessage(outputMessage) {
            if (outputMessage !== undefined && outputMessage !== null) {
                let messageList = document.getElementById('messageList');
                if (messageList.childNodes.length === 1 && messageList.childNodes[0].nodeType === Node.TEXT_NODE) {
                    messageList.innerHTML = '';
                }

                let currentUser = document.getElementById('currentUser');

                let messageBlock = document.createElement('div');

                let messageText = document.createElement('div');
                let messageTextSpan = document.createElement('span');
                messageTextSpan.innerText = outputMessage.text;
                let messageTextI = document.createElement('i');
                messageTextI.innerText = ' ' + outputMessage.tag;

                messageText.appendChild(messageTextSpan);
                messageText.appendChild(messageTextI);

                messageText.classList.add('m-2');

                let messageFooter = document.createElement('div');
                messageFooter.classList.add('card-footer', 'text-muted');
                messageFooter.appendChild(document.createTextNode(outputMessage.author.username));

                if (outputMessage.filename !== undefined && outputMessage.filename !== null) {
                    let messageFile = document.createElement('img');
                    messageFile.src = '/img/' + outputMessage.filename;
                    messageFile.classList.add('card-img-top')
                    messageBlock.appendChild(messageFile);
                }

                messageBlock.appendChild(messageText);
                messageBlock.appendChild(messageFooter);

                if (currentUser.value !== outputMessage.author.username) {
                    messageBlock.classList.add('card', 'my-3', 'chat-message-left', 'pb-4');
                } else {
                    messageBlock.classList.add('card', 'my-3', 'chat-message-right', 'pb-4');
                }

                messageList.appendChild(messageBlock);

                messageList.scrollTop = messageList.scrollHeight
            }
        }

        async function send(text, tag, fileName, fileStr) {
            stompClient.send("/app/chat", {}, JSON.stringify({'text':text, 'tag':tag, 'fileName':fileName, 'fileContent':fileStr}));
            await sleep(200);
        }

        function sleep(ms) {
            return new Promise(resolve => setTimeout(resolve, ms));
        }

        async function sendMessage() {
            let tag = document.getElementById('tag').value;
            let text = document.getElementById('messageText').value;
            let fileStr;
            let reader = new FileReader();
            let file = document.getElementById("validatedCustomFile").files[0];
            if (file) {
                reader.onloadend = async function (evt) {
                    fileStr = evt.target.result;
                    await send(text, tag, file.name, fileStr);
                }
                reader.onerror = function () {
                    alert("Error occurred!")
                }
                reader.readAsDataURL(file);
            } else {
                await send(text, tag);
            }
            document.getElementById('messageText').value = '';
            document.getElementById('tag').value = '';
            document.querySelector(".custom-file-label").innerText = 'Choose file...';
        }

    </script>
    </body>
    </html>
</#macro>