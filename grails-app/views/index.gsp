<!DOCTYPE html>
<html>
	<head>
		<title>ToDo - Welcome</title>
		<link href='http://fonts.googleapis.com/css?family=Sigmar+One' rel='stylesheet' type='text/css'>
		<style>
		h1.mainheading {
			font-family: 'Sigmar One', cursive;
			font-size: 200px;
			text-align: center;
		}
		.clickable {
			cursor: pointer;
		}
		.clickable:hover {
			color: #606060;
		}

		</style>
	</head>
	<body>
		<h1 id="logo" class="mainheading clickable" onclick='window.location="${g.createLink( controller: 'toDo' )}"'>ToDo!</h1>
	</body>
</html>
