<?php

$local_token = "XXXXXX";
$client_token = isset($_SERVER['HTTP_X_GITLAB_TOKEN']) ? $_SERVER['HTTP_X_GITLAB_TOKEN'] : '';

$git_directory = preg_replace('/[^-a-zA-Z0-9\.\/]/', '', $_GET['r']);
$git_operation = "git fetch origin *:*";

if($client_token == $local_token){

	//first send HTTP response (recommended by GitLab)
	ignore_user_abort(true);
	set_time_limit(0);
	ob_start();
	header("HTTP/1.1 200 OK");
	echo "Synchronization of ".$git_directory." initiated.";
	header('Connection: close');
	header('Content-Length: '.ob_get_length());
	ob_end_flush();
	ob_flush();
	flush();

	//then trigger Git operation
	$old_path = getcwd();
	chdir($git_directory);
	$output = shell_exec($git_operation);
	chdir($old_path);

} else{
	header("HTTP/1.1 403 Forbidden");
	echo "Forbidden.";
}


?>
