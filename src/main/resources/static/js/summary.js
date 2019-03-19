// Javascript for a summary table on list view

$(document).ready(function() {
	
	$("#summary").DataTable({
		// Disable sorting on Column 0
		"columnDefs": [ {
		      "targets": [ 0 ],
		      "orderable": false
		} ]
	});
	
});
