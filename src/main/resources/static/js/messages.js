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

async function showMessage(outputMessage) {
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

        messageText.appendChild(messageTextSpan);

        messageText.classList.add('m-2');

        let messageFooter = document.createElement('div');
        messageFooter.classList.add('card-footer', 'text-muted');
        messageFooter.appendChild(document.createTextNode(outputMessage.author.username));

        if (outputMessage.filename !== undefined && outputMessage.filename !== null) {
            let thumbnailBlock = document.createElement('div');
            thumbnailBlock.classList.add('thumbnail');

            let aBlock = document.createElement('a');
            aBlock.href = '/img/' + outputMessage.filename;

            let imageBlock = document.createElement('img');
            imageBlock.src = '/img/' + outputMessage.filename;
            imageBlock.alt = 'Image';
            imageBlock.classList.add('img-responsive');
            imageBlock.width = 250;
            imageBlock.heigth = 250;

            aBlock.appendChild(imageBlock);
            thumbnailBlock.appendChild(aBlock);

            messageBlock.appendChild(thumbnailBlock);
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

async function send(text, fileName, fileStr) {
    stompClient.send("/app/chat", {}, JSON.stringify({'text':text, 'fileName':fileName, 'fileContent':fileStr}));
    await sleep(200);
}

function sleep(ms) {
    return new Promise(resolve => setTimeout(resolve, ms));
}

async function sendMessage() {
    let text = document.getElementById('messageText').value;
    let fileStr;
    let reader = new FileReader();
    let file = document.getElementById("validatedCustomFile").files[0];
    if (file) {
        reader.onloadend = async function (evt) {
            fileStr = evt.target.result;
            await send(text, file.name, fileStr);
        }
        reader.onerror = function () {
            alert("Error occurred!")
        }
        reader.readAsDataURL(file);
    } else {
        await send(text);
    }
    document.getElementById('messageText').value = '';
    document.querySelector(".custom-file-label").innerText = 'Choose file...';
}