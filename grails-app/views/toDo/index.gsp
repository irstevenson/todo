<!DOCTYPE html>
<html>
<head>
    <meta name="layout" content="main"/>
</head>
<body>
<div class="container">
    <div class="page-header">
        <h1>Main Page</h1>
    </div>

    <g:if test="${flash.error}">
        <div class="row"><div class="col-sm-12">
            <div class="alert alert-danger alert-dismissible">
                <button type="button" class="close" data-dismiss="alert" aria-label="Close"><span aria-hidden="true">&times;</span></button>
				<p>${flash.error}</p>
            </div>
        </div></div>
    </g:if>

    <g:if test="${flash.msg}">
        <div class="row"><div class="col-sm-12">
            <div class="alert alert-info alert-dismissible">
                <button type="button" class="close" data-dismiss="alert" aria-label="Close"><span aria-hidden="true">&times;</span></button>
                <p>${flash.msg}</p>
            </div>
        </div></div>
    </g:if>

    <div class="row">
		<div class="col-sm-8">
			<div class="panel panel-default">
				<div class="panel-heading">
					<div class="panel-title">
						<h2>ToDo</h2>
						<h3>${session.currentProjectName}</h3>
					</div>
				</div>
				<div class="panel-body">
					<g:link controller="itemEditor" action="newItem" id="${session.currentProjectId}">
						<button type="button" class="btn btn-primary">
							<span class="glyphicon glyphicon-plus" aria-hidden="true"></span> New Item
						</button>
					</g:link>

					<table class="table">
						<thead>
							<tr>
								<th class="text-center">Pri</th>
								<th class="text-center">Due</th>
								<th>Description</th>
								<th class="text-center">Done</th>
								<th class="text-center">Actions</th>
							</tr>
						</thead>
						<tbody>
							<g:if test="${items.size() < 1}">
								<tr><td colspan="5" class="text-center">add a item</td></tr>
							</g:if>
							<g:each var="details" in="${items}">
								<tr>
									<td class="text-center">${details.item.priority ?: 'None'}</td>
									<td class="text-center">${details.item.dueDate?.format( 'yyyy-MM-dd' ) ?: 'None' }</td>
									<td>${details.item.description}</td>
									<td class="text-center">${details.item.done ? 'YES' : 'NO' }</td>
									<td class="text-center">
										<span class="glyphicon-clickable glyphicon glyphicon-trash"
											onclick='window.location="${g.createLink( action: 'removeItem', id: details.id )}"'></span>
										<span class="glyphicon-clickable glyphicon glyphicon-edit" 
											onclick='window.location="${g.createLink( controller: 'itemEditor', action: 'editItem', id: details.id )}"'></span>
									</td>
								</tr>
							</g:each>
						</tbody>
					</table>
				</div>
			</div>
		</div>
		<div class="col-sm-4">
			<div class="panel panel-default">
				<div class="panel-heading"><div class="panel-title"><h2>Projects</h2></div></div>
				<div class="panel-body">
				<table class="table table-condensed">
					<thead>
						<tr><th class="col-sm-9">Name</th><th class="text-center col-sm-3">Actions</th></tr>
					</thead>
					<tbody>
					<g:if test="${projects.size() < 1}">
						<tr><td colspan="2" class="text-center">add a project</td></tr>
					</g:if>
					<g:each var="project" in="${projects}">
						<tr>
							<td>${project.name} <span class="badge">${project.itemCount}</span></td>
							<td class="text-center">
								<span class="glyphicon-clickable glyphicon glyphicon-trash"
									  onclick='window.location="${g.createLink( action: 'removeProject', id: project.id )}"'></span>
								<span class="glyphicon-clickable glyphicon glyphicon-zoom-in"
									  onclick='window.location="${g.createLink( action: 'viewItems', id: project.id )}"'></span>
							</td>
						</tr>
					</g:each>
					</tbody>
				</table>
				<g:form class="form-inline" name="newProject" action="newProject">
					<div class="form-group">
						<label class="sr-only" for="projectName">new project name</label>
						<g:textField class="form-control" name="projectName" placeholder="new project name"/>
					</div>	
					<button type="submit" class="btn btn-primary">
						<span class="glyphicon glyphicon-plus"></span>
					</button>
				</g:form>
				</div>
			</div>
		</div>
	</div>
</div>
</body>
</html>
