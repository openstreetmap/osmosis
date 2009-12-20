#! /usr/bin/perl -w

print "<document xmlns=\"http://maven.apache.org/changes/1.0.0\"\n";
print "          xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n";
print "          xsi:schemaLocation=\"http://maven.apache.org/changes/1.0.0 http://maven.apache.org/xsd/changes-1.0.0.xsd\">\n";

print "<body>\n";

while (<STDIN>)
{
    chomp;

    if (/^$/)
    {
    }
    elsif (/^0\.\d/)
    {
	# Hack for now...
	print "</release>\n" if (!($_ eq "0.32.1"));
	print "<release version=\"" . $_ . "\" >\n";
    }
    else
    {
	$action = "add";
	$action = "fix"    if (/^Fixed/);
	$action = "update" if (/^Removed/);
	$action = "update" if (/^Switch/);
	$action = "update" if (/^Use/);

	print "<action type=\"" . $action . "\">" . $_ . "</action>\n";
    }
}

print "</body>\n";

print "</document>\n";

