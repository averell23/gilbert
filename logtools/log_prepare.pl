#!/usr/local/bin/perl -w
#
# Log Analizer Script
#

use Utils;
use XML::Writer;
use IO;
use CGI;
use Socket;

@local_net = ("129.13");
@local_domain = ("karlsruhe.de", "uka.de", "teco.edu");
%tld_hash = ( );
%tld_code_hash = ( );
$COUNTRY_CODES_NAME = "country-codes.txt";

($LOGFILE_NAME = shift(@ARGV)) || ($LOGFILE_NAME = "log.txt");
# ($OUTFILE_NAME = shift(@ARGV)) || ($OUTFILE_NAME = "log_out.txt");
($LOGFILE_MODE = shift(@ARGV)) || ($LOGFILE_MODE = "STANDARD");

if (!open(LOGFILE, "$LOGFILE_NAME") ){
	print("Unable to open the logfile: $LOGFILE_NAME");
	exit(1);
}

if (!open(COUNTRIES, "$COUNTRY_CODES_NAME") ){
	print("Unable to open the logfile: $LOGFILE_NAME");
	exit(1);
}

&init_tld();

$writer = new XML::Writer(DATA_MODE => 1, DATA_INDENT => 2, OUTPUT => STDOUT);
$writer->xmlDecl("ISO-8859-1", "yes");
$writer->startTag("visitlist");
while (<LOGFILE>) {
	@line = &Utils::split_logline($_, $LOGFILE_MODE);
	$client = $line[0];
	$referer = $line[7];
	$user_agent = $line[8];
	$document = $line[9];
	$timestamp = $line[3];
	$document =~ s/[^a-zA-Z_0-9:\/\-\.~]//g; # Squasch control characters
	# Try to lookup the client's real name
	# This can be left out if the log contains proper hostnames...
	my $client_addr = inet_aton($client);
	if (defined($client_addr)) {
            my @packet = gethostbyaddr($client_addr, AF_INET);
        }
	if (defined($packet[0])) {
		$client = $packet[0];
	}
	# print "\n\n**********************************\n";
	# print "Client:\t$client\n";
	# print "User Agent:\t$user_agent\n";
	$local = &is_local($client);
	$bot = &is_bot($user_agent);
	($location, $location_code) = &describe_location($client);
	# print "Is Bot: $bot\n";
	# print "Is local: $local\n";
	# print "Location: $location\n";
	$writer->startTag("visit");
	$writer->dataElement("type", "Html");
	$writer->dataElement("timestamp", $timestamp);
	$writer->startTag("visitor");
	if ($local) {
		$writer->dataElement("class", "local");
	} else {
		$writer->dataElement("class", "remote");
	}
	if ($bot) {
		$writer->dataElement("class", "agent");
	} else {
		$writer->dataElement("class", "person");
	}
	$writer->endTag("visitor");
	$writer->dataElement("resource", $document);
	$writer->dataElement("host", $client);
	$writer->dataElement("location", $location);
	$writer->dataElement("location_code", $location_code);
	my @parts = split(/\?/, $referer);
	if (defined($referer) && (!($referer eq "-"))) {
		$writer->startTag("referer");
		$writer->dataElement("url", $referer);
		$writer->dataElement("page_url", $parts[0]);
		$writer->endTag("referer");
	}
	# Check for keywords in the referer field
	if ($referer =~ /.*\?.*/) {
		my $req_query = new CGI($parts[1]);
		my $query_t = $req_query->param("q");
		my @keywords = ( );
		if (defined($query_t)) { push(@keywords, split(/ /, $query_t)) };
		$query_t = $req_query->param("p");
		if (defined($query_t)) { push(@keywords, split(/ /, $query_t)) };
		foreach $keyword (@keywords) {
			$writer->dataElement("keyword", $keyword);
		}
	}
	$writer->endTag("visit");
}
$writer->endTag("visitlist");
$writer->end();

# checks if this is a user agent string of a known bot
sub is_bot {
	my $ua_string = $_[0];
	if ($ua_string =~ /.*crawl.*/i) { return 1 };
	if ($ua_string =~ /.*bot.*/i) { return 1 };
	if ($ua_string =~ /.*gulliver.*/i) { return 1 };
	if ($ua_string =~ /.*scooter.*/i) { return 1 };
	if ($ua_string =~ /.*slurp.*/i) { return 1 };
	if ($ua_string =~ /.*MSIECrawler.*/i) { return 1 };
	if ($ua_string =~ /.*wget.*/i) { return 1 };
	if ($ua_string =~ /.*FlashGet.*/i) { return 1 };
	if ($ua_string =~ /.*w3mir.*/i) { return 1 };
	if ($ua_string =~ /.*GetRight.*/i) { return 1 };
	if ($ua_string =~ /.*Spider.*/i) { return 1 };
	if ($ua_string =~ /.*ia_archiver.*/i) { return 1 };
	return 0;
}

# checks if a host is local
sub is_local {
	my $client = $_[0];
	if (&Utils::is_ip($client)) {
		foreach $net (@local_net) {
			if ($client =~ /\A\s*$net/) { return 1 };
		}
	} else {
		foreach $domain (@local_domain) {
			if ($client =~ /$domain\s*\Z/) { return 1 };
		}
	}
	return 0;
}

# Check the location description and code for that tld
# RETURNS = ($code, $descritpion)
sub describe_location {
	if (! &Utils::is_ip($client)) {
		$_[0] =~ /\.(\w+)\s*\Z/;
		if (defined($1)) {
			# print "**** $1\n";
			my $retval = $tld_hash{uc($1)};
			my $code = $tld_code_hash{uc($1)};
			if (defined($retval)) { return ($retval, $code); }
		}
	}
	return ( "(Unknown Location)", -1 );
}


# init the country code hash
sub init_tld {
	while (<COUNTRIES>) {
		@line = split(/\s+/);
		$tld_code = shift(@line);
		$tld = shift(@line);
		$description = join(' ', @line);
		$tld_hash{$tld} = $description;
		$tld_code_hash{$tld} = $tld_code;
		# print "$tld - $description\n";
	}
}

