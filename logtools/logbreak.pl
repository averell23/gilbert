#!/usr/local/bin/perl -w

# 
# breaks down a logfile into a standardized textfile
# Usage: logbreak.pl <Filename> [<outputfile> [<mode>]]

use Utils;

($LOGFILE_NAME = shift(@ARGV)) || ($LOGFILE_NAME = "log.txt");
($OUTFILE_NAME = shift(@ARGV)) || ($OUTFILE_NAME = "log_out.txt");
($LOGFILE_MODE = shift(@ARGV)) || ($LOGFILE_MODE = "STANDARD");

if (!open(LOGFILE, "$LOGFILE_NAME") ){
	print("Unable to open the logfile: $LOGFILE_NAME");
	exit(1);
}

if (!open(OUTFILE, ">$OUTFILE_NAME")) {
	print("Unable to open output file for writing: $OUTFILE_NAME");
	exit(1);
}

while (<LOGFILE>) {
	@line = &Utils::split_logline($_, $LOGFILE_MODE);
	$line = join("\t", @line);
	print OUTFILE "$line\n";
}
