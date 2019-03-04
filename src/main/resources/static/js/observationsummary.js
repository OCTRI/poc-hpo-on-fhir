// Javascript for the observationsummary table on observation list view

$(document).ready(function() {
	
	$("#observationsummary").DataTable({
		// Disable sorting on Column 0
		"columnDefs": [ {
		      "targets": [ 0 ],
		      "orderable": false
		} ]
	});
	
});
