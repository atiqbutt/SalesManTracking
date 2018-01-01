<?php
 
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
    
 ?>}
