// Javascript for the hposummary table on phenotypes view

$(document).ready(function() {
	
	function getContextPath() {		
		return '/' + window.location.pathname.split('/')[1];
	}
	
	function showLabs(d) {
		// `d` is the original data object for the row
		let table = "<div class='card details-card'>"
				+ "<table class='table-bordered'>"
				+ "<thead>"
				+ "<tr class='table-warning'><th>Resource Id</th><th>LOINC</th><th>Description</th><th>Value</th><th>Start Date</th><th>End Date</th></tr>"
				+ "</thead>" + "<tbody>";
		for (i = 0; i < d.observations.length; i++) {
			let observation = d.observations[i];
			table += "<tr>" + "<td>" + observation.fhirId + "</td><td>"
					+ observation.loincId + "</td><td>"
					+ observation.description + "</td><td>"
					+ observation.value + "</td><td>" 
					+ observation.startDate + "</td><td>" 
					+ observation.endDate+ "</td></tr>";
		}
		table += "</tbody></table></div>";
		return table;
	}
	
	let patientId = $("#patient_id").html();

	// Set up table
	let table = $("#hposummary").DataTable({
		ajax : {
			"url" : getContextPath() + "/summary/" + patientId
		},
		info: true,
		paging : true,
		searching : true,
		fnDrawCallback: function( settings ) {
			$('td.details-control').html('<i class="fas fa-plus-square"></i>');
	    },
		columns : [ 
			{
				"className" : "details-control",
				"orderable" : false,
				"data" : null,
				"defaultContent" : ''
			}, 
			{ "data" : "hpoTermName" }, 
			{ "data" : "hpoTermId" },
			{ "data" : "count"},
			{ "data" : "first" },
			{ "data" : "last"},
			{ 	
				"targets": -1,
				"data" : null,
				"render": function ( data, type, row, meta ) {
				    let checked = (data.hpoObservationId !== null);
				    if (checked) {
				    	return "<input type='checkbox' checked>";
				    } else {
				    	return "<input type='checkbox'>";
				    }
    			}
			}
		],
		order: [[ 2, "asc" ]]
	});
	
	// Add event listener for opening and closing details
	$('#hposummary tbody').on('click', 'td.details-control', function() {
		let tr = $(this).closest('tr');
		let row = table.row(tr);

		if (row.child.isShown()) {
			// This row is already open - close it
			row.child.hide();
			$(this).html('<i class="fas fa-plus-square"></i>');
		} else {
			// Open this row
			row.child(showLabs(row.data()), 'details-row').show();
			$(this).html('<i class="fas fa-minus-square"></i>');
		}
	});
	
	$('#hposummary tbody').on( 'click', 'input[type=checkbox]', function () {
 		var tr = $(this).closest('tr');
		var row = table.row(tr);
        var data = row.data();
        var checkbox = this;
        if (checkbox.checked === true) {
	        $('#CommentsModal').modal("show");
	        $('#CommentsModal-termiddisplay').text(data.hpoTermId);
	        $('#CommentsModal-termid').val(data.hpoTermId);
	        $('#CommentsModal-termnamedisplay').text(data.hpoTermName);
	        $('#CommentsModal-termname').val(data.hpoTermName);
			$('#CommentsModal-negated').val(data.negated);
			$('#CommentsModal-firstdisplay').text(data.first);
			$('#CommentsModal-first').val(data.first);
			$('#CommentsModal-lastdisplay').text(data.last);
			$('#CommentsModal-last').val(data.last);
	        $('#CommentsModal-observations').val(data.observations.map(o => o.fhirId).toString());
	        $('#CommentsModal-comments').val("");
	        $('#CommentsModal-cancel').click(function() {checkbox.checked = false});
	    } else {
	        $('#DeleteModal').modal("show");
	        $('#DeleteModal-title').text("Delete " + data.hpoTermName);
	        $('#DeleteModal-termid').val(data.hpoTermId);
	        $('#DeleteModal-negated').val(data.negated);
	        $('#DeleteModal-cancel').click(function() {checkbox.checked = true});
	    }       
    } );

	$("#CommentsModal-form").submit(function(e) {

	    e.preventDefault();
	
	    var form = $(this);
	    var url = form.attr('action');
	    
	    $.ajax({
	           type: "POST",
	           url: url,
	           data: form.serialize(),
	           success: function(data)
	           {
				   // NOTE: Even after success, the new record may not be queryable in the FHIR server for a few minutes
	               $('#CommentsModal').modal("hide");
	           }
	    });
	    
	});

	$("#DeleteModal-form").submit(function(e) {

	    e.preventDefault();
	
	    var form = $(this);
	    var url = form.attr('action');
	    
	    $.ajax({
	           type: "POST",
	           url: url,
	           data: form.serialize(),
	           success: function(data)
	           {
	               $('#DeleteModal').modal("hide");
	           }
	    });
	    
	});
    
});
