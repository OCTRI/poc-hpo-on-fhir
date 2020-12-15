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
					if (data.needsUpdate) {
					  return "<button class='btn btn-secondary'>Report</button>"
					}
      				return "<i class='far fa-check-square fa-lg text-success'></i>";
    			}
			}
		],
		aaSorting : [] // Do not sort any columns initially
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
	
	$('#hposummary tbody').on( 'click', 'button', function () {
 		var tr = $(this).closest('tr');
		var row = table.row(tr);
        var data = row.data();
        console.log(data);
        $('#CommentsModal').modal("show");
        $('#CommentsModal-title').text("Comments for " + data.hpoTermName);
        $('#CommentsModal-termid').val(data.hpoTermId);
        $('#CommentsModal-termname').val(data.hpoTermName);
        $('#CommentsModal-observations').val(data.observations.map(o => o.fhirId).toString());
        $('#CommentsModal-comments').val("");        
    } );
    
});
