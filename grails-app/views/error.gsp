<!DOCTYPE html>
<html>
	<head>
		<title><g:if env="development">ToDo - Runtime Exception</g:if><g:else>ToDo - Error</g:else></title>
		<meta name="layout" content="main">
		<g:if env="development"><asset:stylesheet src="errors.css"/></g:if>
	</head>
	<body>
        <div class="container">
            <g:if env="development">
                <g:renderException exception="${exception}" />
            </g:if>
            <g:else>
                <ul class="errors">
                    <li>An error has occurred</li>
                </ul>
            </g:else>
        </div>
	</body>
</html>
