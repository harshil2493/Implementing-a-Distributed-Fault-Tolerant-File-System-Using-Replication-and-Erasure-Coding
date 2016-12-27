test_home=/s/chopin/b/grad/hkshah/Submitted555/Replication
 
for i in `cat machine_list`
do
	echo 'logging into '${i}
	gnome-terminal -x bash -c "ssh -t ${i} 'rm -rf /tmp/data; cd $test_home; java node.ChunkServer pierre 5100 34567;bash;'" &
done
