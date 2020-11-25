<?php
$to = urldecode($_REQUEST['to']);
$text = urldecode($_REQUEST['text']);
$secret = urldecode($_REQUEST['secret']);
$token = urldecode($_REQUEST['deviceID']);
//Time required if you use MD5
$time = $_REQUEST['time'];

/**
 * TODO
 * Firebase server key at settings
 * https://console.firebase.google.com/
 */
$firebasekey =  "https://console.firebase.google.com/";

if(isset($_GET['debug']) && count($_REQUEST)>1)
	file_put_contents("log.txt",json_encode($_REQUEST)."\n\n",FILE_APPEND);

if(empty($to) || empty($text) || empty($secret) || empty($token)){
    readfile("info.txt");
    die();
}

$result = sendPush($token,$secret,$time,$to, $text);

if(isset($_GET['debug']) && count($_REQUEST)>1)
	file_put_contents("log.txt",$result."\n\n",FILE_APPEND);
echo $result;

function sendPush($token,$secret,$time,$to, $message) {
    global $firebasekey;
    $url = 'https://fcm.googleapis.com/fcm/send';

    $fields = array (
            'to' => $token,
            'data' => array (
                "to" => $to,
                "time" => $time,
                "secret" => $secret,
                "message" => $message,
            )
    );
    $fields = json_encode ( $fields );

    $headers = array (
            'Authorization: key=' . $firebasekey,
            'Content-Type: application/json'
    );

    $ch = curl_init ();
    curl_setopt ( $ch, CURLOPT_URL, $url );
    curl_setopt ( $ch, CURLOPT_POST, true );
    curl_setopt ( $ch, CURLOPT_HTTPHEADER, $headers );
    curl_setopt ( $ch, CURLOPT_RETURNTRANSFER, true );
    curl_setopt ( $ch, CURLOPT_POSTFIELDS, $fields );

    $result = curl_exec ( $ch );

    curl_close ( $ch );

    return $result;
}
