<#import "parts/common.ftlh" as c>
<#import "parts/security.ftlh" as sec>
<@c.page>
    <div class="form-row">
        <div class="form-group col-md-6">
            <form method="get" action="/main" class="form-inline">
                <input type="text" name="filter" class="form-control" value="${filter!}" placeholder="Search by tag">
                <button type="submit" class="btn btn-primary ml-2">Search</button>
            </form>
        </div>
    </div>
    <div class="position-relative">
        <ul class="list-group list-group-flush chat-messages p-4">
            <#list messages as message>
                <#if sec.name != message.authorName>
                    <div class="card my-3 chat-message-left pb-4">
                        <#if message.filename??>
                            <img src="/img/${message.filename}" class="card-img-top">
                        </#if>
                        <div class="m-2">
                            <span>${message.text}</span>
                            <i>${message.tag}</i>
                        </div>
                        <div class="card-footer text-muted">
                            ${message.authorName} ${sec.name}
                        </div>
                    </div>
                <#else>
                    <div class="card my-3 chat-message-right pb-4">
                        <#if message.filename??>
                            <img src="/img/${message.filename}" class="card-img-top">
                        </#if>
                        <div class="m-2">
                            <span>${message.text}</span>
                            <i>${message.tag}</i>
                        </div>
                        <div class="card-footer text-muted">
                            ${message.authorName} ${sec.name}
                        </div>
                    </div>
                </#if>
            <#else>
                No message
            </#list>
        </ul>
    </div>

    <a class="btn btn-primary" data-toggle="collapse" href="#collapseExample" role="button" aria-expanded="false"
       aria-controls="collapseExample" onclick="connect();">
        Add new message
    </a>
    <div class="collapse" id="collapseExample">
        <div class="form-group mt-3">
            <form enctype="multipart/form-data">
                <div class="form-group">
                    <input id="messageText" type="text" name="text" placeholder="Add message" class="form-control"/>
                </div>
                <div class="form-group">
                    <input id="tag" type="text" name="tag" placeholder="Tag" class="form-control">
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
                <input id="from" type="hidden" value="${sec.name}">
                <input type="hidden" name="_csrf" value="${_csrf.token}">
            </form>
        </div>
    </div>
    <script type="text/javascript">
        document.querySelector('.custom-file-input').addEventListener('change', function (e) {
            var fileName = document.getElementById("validatedCustomFile").files[0].name;
            var nextSibling = e.target.nextElementSibling;
            nextSibling.innerText = fileName
        })
    </script>
</@c.page>