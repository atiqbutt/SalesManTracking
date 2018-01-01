<?php
 define('HOST','localhost');
 define('USER','id1546327_volley');
 define('PASS','12345');
 define('DB','id1546327_paractice');
 
 $con = mysqli_connect(HOST,USER,PASS,DB) or die('Unable to Connect');
 
 if($con){
	 
	 echo"Success.....";
 }
 else{
	 echo"Failed";
	 echo"Failed";
 }
 