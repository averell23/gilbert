#!/usr/local/bin/perl -w

#
# Create a main page with logfile overview information
#

use Utils;
use CGI;
use DBI;

# Just to name all of the global vars that are here...
%ip_hash = (); 	# Hash with all ip adresses from the logfile
$distinct_ips = 0;	# number of distinct ip addresses found
$ips = 0;		# number of ip adresses in the logfile
%hosts_hash = (); 	# Hash with all hostnames from the logfile
$distinct_hosts = 0;	# number of distinct host names found
$hosts = 0;		# number of hostnames in the logfile
%referers_hash = ();	# Hash of all referer information in the lofile
%referers_reverse = (); # Hashes from each referer to the clients served
%referers_reverse_count = (); 	# Counts the number of clients for a referer site
# Create the CGI thingy
$query = CGI::new();


print $query->header();
print $query->start_html(-title=>'Logfile Overview',
                         -style=>{'src'=>'/marco/standard.css'});
# Get a database handle
$dbh = &Utils::open_db();
if (!defined($dbh)) { 
	&Utils::error_message("Could not open database!");
	$query->end_html();
	exit 1;
} else {
	print STDOUT "Database opened o.k.";
	
}

print STDOUT "<h1>General logfile overview</h1>\n";
&analyse_logfile();
&print_summary();
&print_host_info();
&print_ip_info();
&print_referer_info();

print $query->end_html();

# Prints a summary of the logfile information
sub print_summary {
	print STDOUT "<h2>Statistics summary</h2>\n";
	print STDOUT "<table border=\"0\" width=\"100%\">\n";
	print STDOUT "<colgroup span=\"2\">\n";
	print STDOUT "<col align=\"left\">\n";
	print STDOUT "<col align=\"right\">\n";
	print STDOUT "</colgroup>\n";
	print STDOUT "<tr>";
	print STDOUT "<td><b>Number of lines</b></td>\n";
	print STDOUT "<td>$lines</td>\n";
	print STDOUT "</tr><tr>\n";
	print STDOUT "<td><b>Number of ip entries</b></td>\n";
	print STDOUT "<td>$ips</td>\n";
	print STDOUT "</tr><tr>";
	print STDOUT "<td><b>Number of <i>distinct</i> ip addresses</b></td>\n";
	print STDOUT "<td>$distinct_ips</td>\n";
	print STDOUT "</tr><tr>\n";
	print STDOUT "<td><b>Number of host name entries</b></td>\n";
	print STDOUT "<td>$hosts</td>\n";
	print STDOUT "</tr><tr>\n";
	print STDOUT "<td><b>Number of <i>distinct</i> host names</b></td>\n";
	print STDOUT "<td>$distinct_hosts</td>\n";
	print STDOUT "</tr>\n";
	print STDOUT "</table>\n";
}

# Prints information about the ip adresses found
sub print_ip_info {
	# First, sort the ip adresses.
	my @ranked_list = sort { $ip_hash{$b} <=> $ip_hash{$a} } keys %ip_hash;
	print STDOUT "<h2>IP Adresses found in the logfile</h2>";
	print STDOUT "<table border = \"0\" width=\"100%\">\n";
	print STDOUT "<colgroup span=\"3\">\n";
	print STDOUT "<col align=\"left\">\n";
	print STDOUT "<col align=\"left\">\n";
	print STDOUT "<col align=\"right\">\n";
	print STDOUT "</colgroup>\n";
	foreach $ip (@ranked_list) {
		# print STDERR "Printing $ip - $ip_hash{$ip}\n";
		&print_item($ip, $ip_hash{$ip});
	}
	print STDOUT "</table>\n"
	
}

# Prints information about the host names found
sub print_host_info {
	# First, sort the names.
	my @ranked_list = sort { $hosts_hash{$b} <=> $hosts_hash{$a} } keys %hosts_hash;
	print STDOUT "<h2>Host names found in the logfile</h2>\n";
	print STDOUT "<table border = \"0\" width=\"100%\">\n";
	print STDOUT "<colgroup span=\"3\">\n";
	print STDOUT "<col align=\"left\">\n";
	print STDOUT "<col align=\"left\">\n";
	print STDOUT "<col align=\"right\">\n";
	print STDOUT "</colgroup>\n";
	foreach $host (@ranked_list) {
		&print_item($host, $hosts_hash{$host});
	}
	print STDOUT "</table>\n" 
}

# Prints information about referer sites
sub print_referer_info {
	my @ranked_list = sort { $referers_reverse_count{$b} <=> $referers_reverse_count{$a} } keys %referers_reverse_count;
	print STDOUT "<h2>Referer Sites from the logfile</h2>\n";
	print STDOUT "<table border = \"0\" width=\"100%\">\n";
	print STDOUT "<colgroup span=\"3\">\n";
	print STDOUT "<col align=\"left\">\n";
	print STDOUT "<col align=\"left\">\n";
	print STDOUT "<col align=\"right\">\n";
	print STDOUT "</colgroup>\n";
	foreach $refer (@ranked_list) {
		my $params = "referer=$refer";
		my @clist = @{$referers_reverse{$refer}};
		my %chash = ();
		$shortened = "false";
		# print STDERR "Going in ... ";
		for ($i = 0 ; ($#clist >= 0) && ($i <= 100) ; $i++) {
			if ($i == 100) {
				$shortened = "true";
			} else {
				$site = shift(@clist);
				if (!defined($chash{$site})) {
					$params = $params . "&site=$site";
					$chash{$site} = "visited";
				} else {
					$i--;
				}
			}
		}
		# print STDERR "Broke free\n";
		$params = $params . "&short=$shortened";
		print STDOUT "<tr>\n";
		print STDOUT "<td><a href=\"ref_details.pl?$params\">$refer</a></td>\n";
		print STDOUT "<td>$referers_reverse_count{$refer}</td>";
		print STDOUT "</tr>\n";
	}
	print STDOUT "</table>\n";
}

# Prints a single line of the detailed output
sub print_item {
	my $client = $_[0];
	my $number = $_[1];
	my $refnote = "";
	my %refhash = ();
	if (defined($referers_hash{$client})) {
		$refnote = "[Referer Information available]";
	}
	print STDOUT "<tr>\n";
	# <img src= \"indicator.pl?$client\">
	print STDOUT "<td><a href=\"details.pl?client=$client\">$client</a></td>\n";
	print STDOUT "<td>$refnote</td>\n";
	print STDOUT "<td>$number</td>\n";
	print STDOUT "</tr>\n";
}

# This does the main analysis of the logfile
sub analyse_logfile {
	# Create the select statement and go to the database...
	my $sth = &Utils::prepare_where($query, $dbh);
	

	# Ok, let's go through all these lines
	while ($row = $sth->fetchrow_arrayref) {
		$lines++;
		# Now, the first interesting entry is the
		# client adress or hostname. This should be
		# in the first field of each line.
		$client = $row->[0];
		$referer = $row->[1];
		if (!($client eq "-")) { 		# Ignore bad/empty client lines
			if (&Utils::is_ip($client)) {
				# We have an ip address
				if (!defined($ip_hash{$client})) {
					# We've not seen this ip before
					$distinct_ips++;
				}
				$ips++;
				$ip_hash{$client}++;
			} else {
				# Since this isn't an ip, it must be a hostname
				if (!defined($hosts_hash{$client})) {
					# Never seen this hostname before
					$distinct_hosts++;
				}
				$hosts++;
				$hosts_hash{$client}++;
			}
		}
		if (!($referer eq "-")) {		# Note that empty clients go here, too
			if (defined($referers_hash{$client})) {
				if ($#{$referers_hash{$client}} < 500) {
					push(@{$referers_hash{$client}}, $referer);
				}
			} else {
				my @reflist = ( "$referer" );
				# print STDERR "Installing: $reflist[0]\n";
				$referers_hash{$client} = \@reflist;
			}
			my @referer_parts = split(/\//, $referer);
			my $referer_site = $referer_parts[2];
			if (!defined($referer_site)) {
				$referer_site = $referer;
			}
			# print STDERR "******************** $referer_site\n";
			$referers_reverse_count{$referer_site}++;
			if (defined($referers_reverse{$referer_site})) {
				push(@{$referers_reverse{$referer_site}}, $client);
			} else {
				my @clist = ( "$client" );
				$referers_reverse{$referer_site} = \@clist;
			}
		}
	 }
}
