<?php
header("Content-Type: application/json");

/* include token */
$orig_token = "XXXXXX";
$mattermost_token = $_POST['token'];


if($orig_token==$mattermost_token){
  
    $mattermost_text = $_POST['text'];
    $mattermost_user_name = $_POST['user_name'];
    //$mattermost_channel_name = $_POST['channel_name'];
    //$mattermost_channel_id = $_POST['channel_id'];
    //$mattermost_command = $_POST['command'];
    //$$mattermost_response_url = $_POST['response_url'];
    //$$mattermost_team_domain = $_POST['team_domain'];
    //$$mattermost_team_id = $_POST['team_id'];
    //$$mattermost_token = $_POST['token'];
    //$$mattermost_user_id = $_POST['user_id'];

    $links = array();

    $links['your link1'] = "https://www.your-link1.de";
    $links['your link2'] = "https://www.your-link2.de";

    if( array_key_exists ( strtolower($mattermost_text), $links ) ){
       echo '{"response_type": "in_channel", "text": "' . $links[strtolower($mattermost_text)] . '"}';
    } else{
       echo '{"response_type": "in_channel", "text": "Sorry, unknown argument.\nCurrently supported (in lower or upper case): `' . implode(", ",array_keys($links)) . '`"}';
    }

} else {

   echo '{"response_type": "in_channel", "text": "Token mismatch!"}';
   
}
?>
