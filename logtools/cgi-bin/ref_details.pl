#!/usr/local/bin/perl -w

#
# Create a detail page with all refererals for a site
#

use Utils;
use CGI;

$query = CGI::new();

$referer_site = $query->param("referer");

print $query->header();
print $query->start_html(-title=>'Logfile Overview',
                         -style=>{'src'=>'/marco/standard.css'});

if (!($dbh = &Utils::open_db())) {
	&Utils::error_message("Could not open database.");
	print $query->html_end();
	exit 1;
}			 

print("<h1>Referer overview: $referer_site</h1>\n");
print("<h2>List of clients referred by this site:</h2>\n");
print("<ul>\n");

$sth = $dbh->prepare("SELECT client, referer FROM log WHERE referer LIKE \"%$referer_site%\"");
$sth->execute or die("Unexpected SQL error.");
while ($row = $sth->fetchrow_arrayref) {
	$client = $row->[0];
	$referral = $row->[1];
	print("<li><a href=\"details.pl?client=$client\">$client</a>\n");
	print(" (referred through <a href=\"$referral\">$referral</a>)</li>\n");
}
print("</ul>\n");
print $query->end_html();
