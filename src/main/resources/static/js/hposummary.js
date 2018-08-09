// Javascript for the hposummary table on phenotypes view

function showLabs(d) {
	// `d` is the original data object for the row
	let table = "<div class='card float-left'>"
			+ "<table class='table-bordered sub-table'>"
			+ "<thead>"
			+ "<tr class='table-warning'><th>FHIR Id</th><th>LOINC</th><th>Description</th><th>Value</th><th>Date</th></tr>"
			+ "</thead>" + "<tbody>";
	for (i = 0; i < d.observations.length; i++) {
		let observation = d.observations[i];
		table += "<tr>" + "<td>" + observation.fhirId + "</td>" + "<td>"
				+ observation.loincId + "</td>" + "<td>"
				+ observation.description + "</td>" + "<td>"
				+ observation.value + "</td>" + "<td>" + observation.date
				+ "</td>" + "</tr>";
	}
	table += "</tbody></table></div>";
	return table;
}

let patientId = $("#patient_id").html();

$(document).ready(function() {
	// Set up table sorting
	let table = $("#hposummary").DataTable({
		ajax : {
			"url" : "/summary/" + patientId
		},
		info: true,
		paging : true,
		searching : true,
		columns : [ {
			"className" : "details-control",
			"orderable" : false,
			"data" : null,
			"defaultContent" : ''
			}, 
			{ "data" : "hpoTermName" }, 
			{ "data" : "hpoTermId" },
			{ "data" : "count"},
			{ "data" : "first" },
			{ "data" : "last"}
		],
		aaSorting : []
	// Do not sort any columns initially
	});

	// Add event listener for opening and closing details
	$('#hposummary tbody').on('click', 'td.details-control', function() {
		let tr = $(this).closest('tr');
		let row = table.row(tr);

		if (row.child.isShown()) {
			// This row is already open - close it
			row.child.hide();
			tr.removeClass('shown');
		} else {
			// Open this row
			row.child(showLabs(row.data())).show();
			tr.addClass('shown');
		}
	});

});
