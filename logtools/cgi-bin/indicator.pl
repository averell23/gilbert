#!/usr/local/bin/perl -w

use lib "/usr/local/httpd/cgi-bin/";

use Utils;

# We do a simple thing here: The Query string is the adress or whatever
$query = $ENV{"QUERY_STRING"};
# $query = "schnabeltier.schnuller.google.com";

if (&Utils::check_alive($query)) {
	#print "GREEN";
	&showpic("green.gif");
} else {
	# print "RED!";
	if ((!(&Utils::is_ip($query))) && &Utils::check_domains($query)) {
		&showpic("blue.gif");
	} else {
		&showpic("red.gif");
	}
}


sub showpic {
  print "Content-Type: image/gif\n\n";
  open(PIC, $_[0]);
  print <PIC>;
  close(PIC);
}
