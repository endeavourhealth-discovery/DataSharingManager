Run the patches by dragging or copying them into workbench and executing the scripts.

Alternatively...if you are unable to connect to workbench for any reason, they can be run via the command line by doing the below.


*** In a Command Prompt Window type this (changing the filepath, the XXX in -uXXX with your username e.g. -uroot, and the XXX in -pXXX with your password, accordingly):

cmd /K "C:\Program Files\MySQL\MySQL Server 5.7\bin\mysql.exe" –uXXX –pXXX

*** You will get this prompt:
*** mysql>

*** Copy all the Patches from IntelliJ into the "stuff" folder on the C drive (or wherever you want to put them).
*** Paste the below after the mysql> prompt, add in a "source" command, with a semi-colon at the end, for your new, and newly numbered, Patch. 
*** Hit go, it runs them all.
*** You will see "Database changed" appearing once for each Patch.
*** Update, commit and push this file to the DSM repo, along with your new Patch, so that it's easy for anybody else to run all of the patches just by running the below.
*** Cheers!

source c:/stuff/DSMDB_Patch0.sql; source c:/stuff/DSMDB_Patch1.sql; source c:/stuff/DSMDB_Patch2.sql; source c:/stuff/DSMDB_Patch3.sql; source c:/stuff/DSMDB_Patch4.sql; source c:/stuff/DSMDB_Patch5.sql; source c:/stuff/DSMDB_Patch6.sql; source c:/stuff/DSMDB_Patch7.sql;