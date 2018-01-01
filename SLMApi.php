slmAPI<?php
 
 if($_SERVER['REQUEST_METHOD']=='POST'){
 $username = $_POST['username'];
 $password = $_POST['password'];
 
 require_once('dbConnect.php');
 
 $sql = "SELECT * FROM paractice WHERE username = '$username' AND password='$password'  ";
 
 $result = mysqli_query($con,$sql);
 
 $check = mysqli_fetch_array($result);
 
 if(isset($check)){
 echo 'success';
 }else{
 echo 'failure';
 }
 }


 public function update_location()
{

    $latitude=  $this->input->post('latitude'); 
    $longitude = $this->input->post('longitude');
    $salesman_id = $this->input->post('id');

    $this->db->query("UPDATE current_location SET longitude = '$longitude', latitude = '$latitude' WHERE saleman_Id = $salesman_id");
	
}


public function sendMarkedLocations(){
	$salesman_id = $this->input->post('id');
	$latitude=  $this->input->post('latitude'); 
    $longitude = $this->input->post('longitude');
    $image = $this->input->post('image');

    $this->db->query("INSERT INTO `marked_location`(`longitude`, `latitude`, `image`, `salesman_id`) VALUES ('$longitude','$latitude','$image','$salesman_id')");
   }


public function getUpdateInfo(){
	$salesman_id = $this->input->post('id');
	$login = $this->db->query("SELECT * FROM admin WHERE id = '$salesman_id' AND is_delete='0' ")->row_array();
	$office_id = $login['office_id'];
	$group_id = $login['group_id'];
	$units= $this->db->query("SELECT * FROM `group` WHERE id = '$group_id ' AND is_delete='0' ")->row_array();


	$username = $login['username'];
    $coordinates = $office['coordinates'];
    $time_in = $office['time_in'];
    $time_out = $office['time_out'];
    $rate = $units['rate']. ' ' .$units['currency'];


    $response = array();

    array_push($response, array('coordinates'=>$coordinates, 'time_in'=>$time_in, 'time_out'=>$time_out, 'rate'=>$rate,'username'=>$username));
    echo json_encode($response);
}

    
 ?>
