<?php
defined('BASEPATH') OR exit('No direct script access allowed');

class API extends CI_Controller {

  public function __construct()
  {
    parent::__construct();
  }

public function login()
          {

  $username=   $this->input->post('username'); 
  $password =  md5($this->input->post('password'));
  $mobile=     $this->input->post('phone');
 
  //$username=  'abid123';
  //$password =  md5('123');
  //$mobile=     '03004728806';

   $login = $this->db->query("SELECT * FROM admin WHERE username = '$username' AND password = '$password' AND mobile= '$mobile' AND is_delete='0' ")->row_array();

    $office_id = $login['office_id'];

    $office = $this->db->query("SELECT * FROM office WHERE id = '$office_id' AND is_delete='0'")->row_array();

    $group_id = $login['group_id'];
    
    $units= $this->db->query("SELECT * FROM `group` WHERE id = '$group_id ' AND is_delete='0' ")->row_array();

    //var_dump($units);  die();
   
    $response = array();

       if($login) {

         if($login['is_login'] == 1 ){

           $msg = "already";
           array_push($response, array('message'=>$msg ));
           echo json_encode($response);
           die();

         }else{
         
          $msg = "true";
          $id= $login['id'];
          $coordinates = $office['coordinates'];
          $time_in = $office['time_in'];
          $time_out = $office['time_out'];
          $rate = $units['name'];
          
          array_push($response, array('id'=>$id, 'coordinates'=>$coordinates, 'time_in'=>$time_in, 'time_out'=>$time_out, 'message'=>$msg, 'rate'=>$rate ));

             echo json_encode($response);
           }

           $this->db->query("UPDATE admin SET is_login = '1' WHERE id = $id ");

           $this->db->query("INSERT INTO current_location(longitude, latitude, saleman_id) VALUES ('0', '0', '$id') ");
   
         }else{

             $msg = "false";
             array_push($response, array('message'=>$msg ));
             echo json_encode($response);
       }
   
 }



public function update_location()
{

    $latitude=   $this->input->post('latitude'); 
    $longitude = $this->input->post('longitude');
    $salesman_id =  $this->input->post('id');

	$this->db->query("UPDATE current_location SET longitude = $longitude, latitude = $latitude WHERE saleman_Id = $salesman_id");
	
}


public function stay_info()
{
    $start_time =   $this->input->post('start_time'); 
    $end_time  =    $this->input->post('end_time');
    $duration =     $this->input->post('duration');
    $coordinates =  $this->input->post('latitude').",".$this->input->post('longitude'); 
    $salesman_id =  $this->input->post('id');

	$this->db->query("INSERT INTO stay_info (start_time, end_time, duration, coordinates, salesman_id) VALUES ( '$start_time', '$end_time', '$duration', '$coordinates', '$salesman_id')");

	
}


public function check_in(){

  $CheckInTime = $this->input->post('checkIn');
  $salesman_id =  $this->input->post('id');
  $Currentdate = date("Y-m-d");
  $this->db->query("INSERT INTO attendance (checkin_time,Day,saleman_id) VALUES ( '$CheckInTime', '$Currentdate', '$salesman_id')");

}

public function check_out(){

  $CheckOutTime = $this->input->post('checkOut');
  $auto = $this->input->post('auto');
  $latitude=   $this->input->post('latitude'); 
  $longitude = $this->input->post('longitude');
  $salesman_id =  $this->input->post('id');
  $Currentdate = date("Y-m-d");

  $this->db->query("UPDATE attendance SET checkout_time = $CheckOutTime, checkout_lng = $longitude, checkout_lat = $latitude, autocheckout = $auto WHERE saleman_Id = $salesman_id AND Day = $Currentdate");

}

public function distanceAtCheckIn(){
  $salesman_id =  $this->input->post('id');
  $date = date("Y-m-d");
  $this->db->query("INSERT INTO traveled_distance (distance,day_allowance,salesman_id,day) VALUES ( 0, ,0,'$salesman_id','$date')");
}


}

public function updateDistance(){

  $salesman_id =  $this->input->post('id');
  $date = date("Y-m-d");
  $distance  =  $this->input->post('distance');
  $day_allowance =  $this->input->post('allowance');
  $this->db->query("UPDATE traveled_distance SET distance = '$distance', day_allowance = '$day_allowance' WHERE salesman_id = '$salesman_id' AND day = '$date'");
}

/* End of file API.php */
/* Location: ./application/controllers/API.php */