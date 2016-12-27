Name 	: Shah, Harshil
CSUID 	: 830387790
(For better looking of ReadMe, open it in gedit (Keep It Maximized))

Index
-----

1. 	Included Files
2. 	How To Run
3. 	Important Notes
4. 	Class Description


1. 	Included Files
--	--------------
(a)	All Source Java Files Included Inside Packages Shown Below ( Folder Replication )
		package node
		package protocols
		package transport
		package util
(b)	Makefile
(c)	ReadMe


2.	How To Run
--	----------
Run Controller by java node.Controller and give port number which we want Controller to listen to.
Run ChunkServer by java node.ChunkServer by giving host address and port number of Controller as argument. Furthermore, We have to specify its port too.
Run Client by java node.client and give host address and port number of Controller as argument.

3. 	Important Notes
--	---------------
	Commands
	--------------------------
	store : 
	-------	
3	In client's console, write "store FILENAME" to upload file to cluster
	
	Read: 
	-----
	In client's console, write "read FILENAME" to read file from cluster

