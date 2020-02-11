$( document ).ready( function() {
    $( "tbody tr" ).each(
        function( index, row ) {
            if ( row.cells.length == 5 ) {
                var column = $( row ).children()[ 0 ];
                if ( index % 2 == 0 ) {
                    var app = $( column ).text();

                    $.each( [ "api", "swagger-ui.html" ], function( index, value ) {
                        var url = "/" + app + "/" + value;
                        $.ajax({
                            complete: function( xhr, textStatus ) {

                                var swaggerColumn = $( row ).children()[ 4 ];
                                if ( xhr.status == 200 ) {
                                    var a = "<a href = '" + url + "' target = '_blank'>API</a>";
                                    $( swaggerColumn ).html( a );
                                }
                            },
                            url: url
                        });
                    });
                }
                else {
                    $( column ).attr( "colspan", row.cells.length );
                }
            }
        }
    );
});
