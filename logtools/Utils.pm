package Utils;

use lib "/usr/lib/perl5/site_perl/5.6.0/";
use lib "/usr/lib/perl5/site_perl/5.6.0/i586-linux/";

require LWP::UserAgent;
require HTTP::Request;
require HTTP::Response;

require Socket;
require Sys::Hostname;
require LWP;
require DBI;

$HTTP_PROXY = "http://wwwcache.lancs.ac.uk:8080";
$CONFIG_FILE_NAME ="marco.conf";

# Create a object to send out the query
sub create_ua {
	$ua = new LWP::UserAgent;
	$ua->proxy(['http'], $HTTP_PROXY);
	$ua->timeout(10);
}

# Checks whether a server is alive
sub check_alive {
  &create_ua();
  $url = "http://" . $_[0] . "/";
  # print "Checking $url\n";
  my $req = new HTTP::Request HEAD => $url;
  my $res = $ua->request($req);
  return $res->is_success;
}

# Checks whether there is a (sub-) domain that is alive
sub check_domains {
	$host = $_[0];
	# print "CHECKING DOMAINS for $host\n";
	@parts = split(/\./, $host);
	# print "Parts: $#parts\n";
	for ( ; $#parts > 0 ; shift(@parts)) {
		# print "CHECKING www." .join('.', @parts) ."\n";
		if (&check_alive("www." . join('.', @parts))) {
			# print "FOUND!\n";
			return 1;
		}
	}
	return 0;
}

# Determines wether the given string is an ip addy
sub is_ip {
	return $_[0] =~ /\A(\d{1,3}\.){3}\d{1,3}\Z/;
}

# Splits up a line of the logfile into a generic list
# format returned: ($client, $rfc1413ident, $username, $timestamp, $request, $status, $size, $referer, $user_agent, $document)
sub split_logline {
	my $line = $_[0];
	my $mode = $_[1];
	# Init them to the dummy hyphen
	my $client = "-";
	my $identity = "-";
	my $username = "-";
	my $timestamp = "-";
	my $request = "-";
	my $status = "-";
	my $size = "-";
	my $user_agent = "-";
	my $referer = "-";
	my $document = "-";
	my $lon = 0;
	if ($mode eq "STANDARD") {
		# Parse the standard log format
		if ($line =~ /(\S+)\s(\S+)\s(\S+)\s(\[.*\])\s(\".*\")\s(\S+)\s(\S+)/) {
			$client = $1;
			$identity = $2;
			$username = $3;
			$timestamp = &convert_date($4);
			$request = $5;
			$status = $6;
			$size = $7;
			# print STDERR $size . "\n";
		} else {
			print STDERR "Error parsing line in STANDARD format:\n";
			print STDERR $line . "\n";
		}
	} elsif ($mode eq "ALBRECHT") {
		# Parse Albrecht's self-styled log format
		if ($line =~ /(.*)\s(\S+)\s->\s(\S+)\s(\S+)\s(\S+)\s(\S+)\s(\[.*\])\s(\".*\")\s(\S+)\s(\S+)/) {
			$user_agent = $1;
			$referer = $2;
			$document = $3;
			$client = $4;
			$identity = $5;
			$username = $6;
			$timestamp = &convert_date($7);
			$request = $8;
			$status = $9;
			$size = $10;
			# print STDERR $size . "\n";
		} else {
			print STDERR "Error parsing line in ALBRECHT's format\n";
			print STDERR $line . "\n";
		}
	} elsif ($mode eq "SANE") {
		# Parse the sane format, where all fields are in the correct
		# order and tab-separated.
		@line = split(/\t/, $line);
		return @line;
	} else {
		print STDERR "Unknown mode: $mode\n";
	}
	return ($client, $identity, $username, $timestamp, $request, $status, $size, $referer, $user_agent, $document)
}

# Prints a generic error message
sub error_message {
	print STDOUT "<h1>Internal Skript message</h1>\n";
	print STDOUT "The skript has encountered an internal error, and cannot continue<br>\n";
	print STDOUT "<p>\n";
	print STDOUT $_[0] . "\n";
	print STDOUT "</p>\n";
}

# Opens the Database with the values given in the configfile
sub open_db {
	my %configs = ();
	if (open(CONFIGFILE, "$CONFIG_FILE_NAME")) {
		while (<CONFIGFILE>) {				# FIXME: Config Filer reader sucks big time...
			@dummy = split(/;/);
			@parts = split(/=/, $dummy[0]);
			$configs{$parts[0]} = $parts[1];
		}
		# print STDERR "Read: " . $configs{"server"} . "/" . $configs{"user"} . "/" . $configs{"password"};
		return DBI->connect("DBI:mysql:logs:" . $configs{"server"}, $configs{"user"}, $configs{"password"});
	} else {
		print STDERR "Unable to open config file $CONFIG_FILE_NAME";
		return;
	}
	close(CONFIGFILE);
}

# Prepares the WHERE clause that selects the records out of the database
# from a given query...
# Param 0: Reference to a query object
# Param 1: Database handle
# Param 2: Additional WHERE option
sub prepare_where {
	$query = $_[0];
	$dbh = $_[1];
	$additional = $_[2];
	my $noerror = $query->param("noerror");
	my $nopics = $query->param("nopics");
	my $nolocal = $query->param("nolocal");
	my $nolocalref = $query->param("nolocalref");
	my $nobots = $query->param("nobots");
	my $client_include = $query->param("client_include");
	my $client_exclude = $query->param("client_exclude");
	my $referer_include = $query->param("referer_include");
	my $referer_exclude = $query->param("referer_exclude");
	my $document_include = $query->param("document_include");
	my $document_exclude = $query->param("document_exclude");
	my @where = ();
	if (defined($additonal) && !($additional eq "")) {
		push(@where, $additional);
	}
	if (defined($noerror) && $noerror eq "true") {
		my $sth = $dbh->prepare("SELECT opt_val FROM options WHERE opt_key=\"select_errors\"");
		$sth->execute or die "Unexpected SQL error";
		$row = $sth->fetchrow_arrayref;
		push(@where, "NOT (" . $row->[0] . ")");
	}
	if (defined($nopics) && $nopics eq "true") {
		my $sth = $dbh->prepare("SELECT opt_val FROM options WHERE opt_key=\"select_pictures\"");
		$sth->execute or die "Unexpected SQL error";
		$row = $sth->fetchrow_arrayref;
		push(@where, "NOT (" . $row->[0] . ")");
	}
	if (defined($nolocal) && $nolocal eq "true") {
		my $sth = $dbh->prepare("SELECT opt_val FROM options WHERE opt_key=\"select_local\"");
		$sth->execute or die "Unexpected SQL error";
		$row = $sth->fetchrow_arrayref;
		push(@where, "NOT (" . $row->[0] . ")");
	}
	if (defined($nolocalref) && $nolocalref eq "true") {
		my $sth = $dbh->prepare("SELECT opt_val FROM options WHERE opt_key=\"select_local_ref\"");
		$sth->execute or die "Unexpected SQL error";
		$row = $sth->fetchrow_arrayref;
		push(@where, "NOT (" . $row->[0] . ")");
	}
	if (defined($nobots) && $nobots eq "true") {
		my $sth = $dbh->prepare("SELECT opt_val FROM options WHERE opt_key=\"select_bots\"");
		$sth->execute or die "Unexpected SQL error";
		$row = $sth->fetchrow_arrayref;
		push(@where, "NOT (" . $row->[0] . ")");
	}
	if (defined($client_include) && !($client_include eq "")) {
		push(@where, "client LIKE \"$client_include\"");
	}
	if (defined($client_exclude) && !($client_exclude eq "")) {
		push(@where, "client NOT LIKE \"$client_exclude\"");
	}
	if (defined($referer_include) && !($referer_include eq "")) {
		push(@where, "referer LIKE \"$referer_include\"");
	}
	if (defined($referer_exclude) && !($referer_exclude eq "")) {
		push(@where, "referer NOT LIKE \"$referer_exclude\"");
	}
	if (defined($document_include) && !($document_include eq "")) {
		push(@where, "document LIKE \"$document_include\"");
	}
	if (defined($document_exclude) && !($document_exclude eq "")) {
		push(@where, "document NOT LIKE \"$document_exclude\"");
	}
	my $where = join(" AND ", @where);
	if (defined($where) && !($where eq "")) {
		$where = " WHERE $where";
	}
	my $select = "SELECT client, referer FROM log$where";
	print "<h2>SELECT statement for this data</h2>\n";
	print "<p>$select</p>\n";
	my $sth = $dbh->prepare($select);
	$sth->execute or die "Unexpected SQL error";
	return $sth;
}

#
# Takes a logfile date string and returns it in the 
# format: DD:MM:YYYY:hh:mm:ss
# (Implementation could be more elegant...)
sub convert_date {
	my $org_date = $_[0];
	$org_date =~ s/[\[\]]//g; #
	my @tmp_org_l = split(/ /, $org_date);
	$org_date = $tmp_org_l[0];
	my @d_list = split(/:/, $org_date); # Split all parts
	my $date_s = shift(@d_list); # Get the first part (date)
	my @ds_list = split(/\//, $date_s); # Split the date string
	my $day = shift(@ds_list); 
	my $month = &month_number(uc(shift(@ds_list)));
	my $year = shift(@ds_list);
	my @date = ($day, $month, $year);
	unshift(@d_list, @date); # move date in 
	# print "Date converted to: " . join(':', @d_list) . "\n";
	return join(':', @d_list);
}

#
# Converts a month in String format to a number
# FIXME: Awkward implementation
#
sub month_number {
	my $m_name = $_[0];
	my %m_map = (
		JAN => 1,
		FEB => 2,
		MAR => 3,
		APR => 4,
		MAY => 5,
		JUN => 6,
		JUL => 7,
		AUG => 8,
		SEP => 9,
		OKT => 10,
		NOV => 11,
		DEC => 12,
	);
	$m_name = $m_map{uc($m_name)};
	if (defined($m_name)) {
		return $m_name;
	} else {
		return 0;
	}
}
