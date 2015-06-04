<!DOCTYPE html>
<html>
<head>
    <meta name="layout" content="main"/>
</head>
<body>
<div class="container">
    <div class="page-header">
        <h1>Edit Item</h1>
    </div>

    <g:if test="${flash.error}">
        <div class="row"><div class="col-sm-12">
            <div class="alert alert-warning alert-dismissible">
                <button type="button" class="close" data-dismiss="alert" aria-label="Close">
					<span aria-hidden="true">&times;</span>
				</button>
				${flash.error}
            </div>
        </div></div>
    </g:if>
	<g:if test="${flash.itemDetails?.hasErrors()}">
		<div class="row"><div class="col-sm-12">
			<div class="alert alert-warning alert-dismissible">
				<p>The data provided was incorrect:</p>
				<g:renderErrors bean="${flash.itemDetails}"/>
			</div>
		</div>
	</g:if>

    <div class="row">
        <div class="col-sm-6">
            <g:form name="itemDetails" controller="itemEditor" action="saveItem" id="${targetItemId}" class="form-horizontal">
			<input type="hidden" name="projectId" value="${targetProjectId}">
               <div class="form-group">
                   <label for="description" class="col-sm-3 control-label">Description</label>
                   <div class="col-sm-9">
                       <g:textField name="description" class="form-control"
					   placeholder="Description" value="${itemDetails?.description}"/>
                   </div>
               </div>
                <div class="form-group">
                   <label for="dueDate" class="col-sm-3 control-label">Due Date</label>
                   <div class="col-sm-4">
                       <div class='input-group date' id='projectDate'>
                           <g:textField name="dueDate" class="form-control" data-date-format="YYYY/MM/DD" readonly="false" value="${itemDetails?.dueDate?.format( 'yyyy/MM/dd' )}"/>
                           <span class="input-group-addon">
                               <span class="glyphicon glyphicon-calendar"></span>
                           </span>
                       </div>
                   </div>
               </div>
               <div class="form-group">
               		<label for="done" class="col-sm-3 control-label">Done</label>
                   	<div class="col-sm-9">
                      	<input type="checkbox" id="done" name="done" ${itemDetails?.done ? 'checked'
						: '' }/>
                   	</div>
               </div>
               <div class="form-group">
				   <div class="col-sm-offset-3 col-sm-9">
					   <g:submitButton name="save" class="btn btn-primary" value="Save"/>
					   <g:link controller="toDo" action="index"><button type="button" class="btn btn-default">Cancel</button></g:link>
				   </div>
               </div>
            </g:form>
        </div>
    </div>
</div>
<script type="text/javascript">
    $(function () {
        $('#projectDate').datetimepicker({
            pickTime: false
        });

        // Turn on tool tips
        $('[data-toggle="tooltip"]').tooltip();
    });
</script>
</body>
</html>
