#!/usr/local/bin/perl -w

#
# Create a detailled page with information on a single ip/hostname
#

use Utils;

$client = "UNDEFINED CLIENT";
@reflist = ();
$params = $ENV{"QUERY_STRING"};
# $params = "client=marvin.uni-dortmund.de&referers=alpha,beta";
@param_list = split(/\&/, $params);
foreach $pstring (@param_list) {
	my @parts = split(/\=/, $pstring);
	if ($parts[0] eq "client") {
		$client = $parts[1];
	} elsif ($parts[0] eq "referers") {
		shift(@parts);
		$refstr = join('=', @parts);
		@reflist = split(/\,/, $refstr);
	}
}

&Utils::html_header("Detail Overview");
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
} else {
	print "This looks like a <b>hostname</b>\n";
	&print_host_details();
}
&Utils::html_footer();

# Prints details for an ip address
sub print_ip_details {
	print "<h2>IP Address Analysis</h2>\n";
	print "Sorry, no analysis available at this time.\n"
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
	if ($#reflist > -1) {
		print "<li><b>Referers</b></li>\n";
		print "<ul>\n";
		foreach $ref (@reflist) {
			print "<li><b>Referer:</b> <a href=\"$ref\">$ref</a></li>\n";
		}
	}
	print "</ul>\n";
	print "</ul>\n";
}
