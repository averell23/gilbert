#!/usr/local/bin/perl -w

#
# Does a traceroute to the given site
#

use Utils;
use CGI;

$TRACEROUTE_CMD="/usr/sbin/traceroute";
$query = CGI::new();
$target = $query->param("target");

print $query->header();
print $query->start_html(-title=>'Traceroute',
                         -style=>{'src'=>'/marco/standard.css'});
if (open(TRACER, "$TRACEROUTE_CMD  $target |")) {
	print("<h2>Tracing $target</h2>\n");
	print("<pre>\n");
	while (<TRACER>) {
		print("$_");
	}
	print("</pre>");
} else {
	&Utils::error_message("Sorry, could not call external traceroute.");
}
print $query->end_html();
