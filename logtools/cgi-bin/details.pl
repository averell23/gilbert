#!/usr/local/bin/perl -w

#
# Create a detailled page with information on a single ip/hostname
#

use Utils;
use CGI

$query = CGI::new();

$client = $query->param("client");

if (!($dbh = &Utils::open_db())) {
	&Utils::error_message("Could not open database.");
	print $query->html_end();
	exit 1;
}

print $query->header();
print $query->start_html(-title=>'Detail Overview',
                         -style=>{'src'=>'/marco/standard.css'});
print "<h1>Details for $client</h1>\n";
if (&Utils::check_alive($client)) {
	print "This client is <b>alive.</b>";
	print " <a href=\"http://$client/\">[Click to connect]</a><br>\n";
} else {
	print "This client is <b>not alive</b><br>\n";
}
print "<p><a href=\"traceroute.pl?target=$client\">[Click here for traceroute]</a></p>";
if (&Utils::is_ip($client)) {
	print "This looks like an <b>IP address</b>\n";
	&print_ip_details();
} else {
	print "This looks like a <b>hostname</b>\n";
	&print_host_details();
}
print $query->end_html();

# Prints details for an ip address
sub print_ip_details {
	print "<h2>IP Address Analysis</h2>\n";
	print "<ul>\n";
	print "<li><b>IP Address:</b> $client ";
	print "<a href=\"traceroute.pl?target=$client\">[traceroute]</a></li>\n";
	&print_referers();
	&print_documents();
	print "</ul>\n";
}

# Prints details for a hostname
sub print_host_details {
	print "<h2>Hostname Analysis</h2>\n";
	my @parts = split(/\./, $client);
	$toplevel = pop(@parts);
	push(@parts, $toplevel);
	print "<ul>\n";
	print "<li><b>Top Level Domain:</b> $toplevel</li>\n";
	print "<li><b>Subdomains</b></li>";
	print "<ul>\n";
	for ( ; $#parts > 0 ; shift(@parts)) {
		my $current = "www." . join('.', @parts);
		print "<li><b>Subdomain site:</b> <a href=\"http://$current/\">$current</a> ";
		print "<a href=\"traceroute.pl?target=$current\">[traceroute]</a> ";
		if (&Utils::check_alive($current)) {
			print " (Site is alive)</li>\n";
		} else {
			print " (Site is not alive)</li>\n";
		}
	}
	print "</ul>\n";
	&print_referers();
	&print_documents();
	print "</ul>\n";
}

sub print_referers {
	$sth = $dbh->prepare("SELECT DISTINCT referer FROM log WHERE client=\"$client\" AND referer != \"-\"");
	$sth->execute or die("Unexpected SQL error.");
	if ($row = $sth->fetchrow_arrayref) {
		print "<li><b>Referers</b></li>\n";
		print "<ul>\n";
		my $ref = $row->[0];
		print "<li><b>Referer:</b> <a href=\"$ref\">$ref</a></li>\n";			
		while ($row = $sth->fetchrow_arrayref) {
			$ref = $row->[0];
			print "<li><b>Referer:</b> <a href=\"$ref\">$ref</a></li>\n";
		}
		print "</ul>\n";
	}
}

sub print_documents {
	$sth = $dbh->prepare("SELECT DISTINCT document, COUNT(*) AS number FROM log WHERE client=\"$client\" AND document != \"-\" GROUP BY document ORDER BY number DESC");
	$sth->execute or die("Unexpected SQL error.");
	print "<li><b>Documents</b></li>\n";
	print "<ul>\n";
	while ($row = $sth->fetchrow_arrayref) {
		$doc = $row->[0];
		$count = $row->[1];
		print "<li><b>Fetched $count times:</b> $doc</li>\n";
	}
	print "</ul>\n";
}
