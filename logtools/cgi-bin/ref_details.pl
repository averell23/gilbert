#!/usr/local/bin/perl -w

#
# Create a detail page with all refererals for a site
#

use Utils;

$params = $ENV{"QUERY_STRING"};
@param_list = split(/\&/, $params);
@sitelist = ();
foreach $pstring (@param_list) {
	my @parts = split(/\=/, $pstring);
	if ($parts[0] eq "referer") {
		$referer_site = $parts[1];
	} elsif ($parts[0] eq "site") {
		shift(@parts);
		push(@sitelist, join('=', @parts));
	} elsif ($parts[0] eq "short") {
		$short = $parts[1];
	}
}
&Utils::html_header("Referer Site overview");
print("<h1>Referer overview: $referer_site</h1>\n");
print("<h2>List of clients referred by this site:</h2>\n");
print("<ul>\n");
foreach $site (@sitelist) {
	print("<li><a href=\"details.pl?client=$site\">$site</a></li>\n");
}
if ($short eq "true") {
	print("<p><b>This list has been shortened to allow CGI handling.</b></p>\n");
}
print("</ul>\n");
&Utils::html_footer();
