#!/usr/bin/perl 

use strict;
use warnings;
use IO::File;

my $project_name="openstreetmap-osmosis";

my $fi = IO::File->new("svn log .|");
my $fo = IO::File->new(">debian/changelog");
my ($rev,$user,$date,$lines) = ('','','','');
my $user2full_name={
    joerg => 'Joerg Ostertag (Debian Packages) <debian@ostertag.name>',
};

sub print_user_line(){
    if ( $user ) {
	my $full_name = $user2full_name->{$user};
	die "Unknown User translation for $user " if ! $full_name;
	
	print $fo "\n -- $full_name   $date\n\n";
    }
}

while ( my $line = $fi->getline()) {
    if ( $line =~ m/^-+$/) {
	print_user_line();
	$line = $fi->getline();
	($rev,$user,$date,$lines) = split(m/\s*\|\s*/,$line);
	$rev =~ s/^r//;

	print $fo "$project_name ($rev) unstable; urgency=low\n\n";
    } elsif ( $line=~ m/^\s*$/ ) {
#	print $fo "\n";	
    } else {
	print $fo "   * $line";
    }
};
print_user_line();

$fi->close();
$fo->close();

__END__
# Example:

openstreetmap-osmosis (7572) unstable; urgency=low

  * Initial Version
  
 -- Joerg Ostertag (Debian Packages) <debian@ostertag.name>  Fri, 1 Jan 2007 07:05:36 +0100

