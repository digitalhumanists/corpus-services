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


    $destination = 'https://your.redmine.server/redmine/issues.json';

    $data = array(
        'issue' => array(
                'project_id'     => 'inel',
                'subject'        => $mattermost_text,
                'description'    => 'Issue created via Mattermost by '.$mattermost_user_name,
                'priority_id'    => 2,
                'tracker_id'     => 5,
                'watcher_ids'    => array( XX ),
                'assigned_to_id' => XX
        )
    );

    file_get_contents(
        $destination,
        false,
        stream_context_create(array(
          'http' => array(
                'method' => 'POST',
                'content' => json_encode( $data ),
                'header'=>  "Content-Type: application/json\r\n" .
                            "Accept: application/json\r\n" .
                            "X-Redmine-API-Key: XXXXXX\r\n"
          )
        )
    ));


    // Don't know how to get the body (JSON response of Redmine API) of the HTTP response
    //$response = var_dump($result);

    // $http_response_header contains HTTP headers of response, and at position 10 in the array there is "Location: ..."
    echo '{"response_type": "in_channel", "text": "Thanks, @'. $mattermost_user_name .'. Issue is created with subject &quot;' . $mattermost_text . '&quot;: '. str_replace("Location: ", "", $http_response_header[10]) .'"}';


} else {

   echo '{"response_type": "in_channel", "text": "Token mismatch!"}';
   return false;

}
?>
