package Utils;

use lib "/usr/lib/perl5/site_perl/5.6.0/";
use lib "/usr/lib/perl5/site_perl/5.6.0/i586-linux/";

require LWP::UserAgent;
require HTTP::Request;
require HTTP::Response;

require Socket;
require Sys::Hostname;
require LWP;

$HTTP_PROXY = "http://wwwcache.lancs.ac.uk:8080";

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
			$timestamp = $4;
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
			$timestamp = $7;
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
