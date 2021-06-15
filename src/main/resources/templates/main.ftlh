<#import "parts/common.ftlh" as c>
<#import "parts/security.ftlh" as sec>
<@c.page>
    <div class="form-row">
        <div class="form-group col-md-6">
            <form method="get" action="/main" class="form-inline">
                <input type="text" name="filter" class="form-control" value="${filter!}" placeholder="Search">
                <button type="submit" class="btn btn-primary ml-2">Search</button>
            </form>
        </div>
    </div>
    <div class="position-relative">
        <ul id="messageList" class="list-group list-group-flush chat-messages p-4">
            <#list messages as message>
                <#if sec.name != message.authorName>
                    <div class="card my-3 chat-message-left pb-4">
                        <#if message.filename??>
                            <div class="thumbnail">
                                <a id="fullPath" href="/img/${message.filename}">
                                    <img id="imageBox" src="/img/${message.filename}" alt="Image" class="img-responsive" width="250" height="250">
                                </a>
                            </div>
                        </#if>
                        <div class="m-2">
                            <span>${message.text}</span>
                        </div>
                        <div class="card-footer text-muted">
                            ${message.authorName}
                        </div>
                    </div>
                <#else>
                    <div class="card my-3 chat-message-right pb-4">
                        <#if message.filename??>
                            <div class="thumbnail">
                                <a href="/img/${message.filename}">
                                    <img src="/img/${message.filename}" alt="Image" class="img-responsive" width="250" height="250">
                                </a>
                            </div>
                        </#if>
                        <div class="m-2">
                            <span>${message.text}</span>
                        </div>
                        <div class="card-footer text-muted">
                            ${message.authorName}
                        </div>
                    </div>
                </#if>
            <#else>
                No message
            </#list>
        </ul>
        <div class="form-group mt-3">
            <div class="form-group">
                <input id="messageText" type="text" name="text" placeholder="Add message" class="form-control"/>
            </div>
            <div class="form-group">
                <div class="custom-file">
                    <input type="file" name="file" class="custom-file-input" id="validatedCustomFile">
                    <label class="custom-file-label" for="validatedCustomFile">Choose file...</label>
                </div>
            </div>
            <div class="form-group">
                <button type="submit" class="btn btn-primary" onclick="sendMessage();">Send</button>
            </div>
            <input id="currentUser" type="hidden" value="${sec.name}">
            <input type="hidden" name="_csrf" value="${_csrf.token}">
        </div>
    </div>

    <script type="text/javascript">
        document.querySelector('.custom-file-input').addEventListener('change', function (e) {
            let fileName = document.getElementById("validatedCustomFile").files[0].name;
            let nextSibling = e.target.nextElementSibling;
            nextSibling.innerText = fileName
        })
    </script>
</@c.page>