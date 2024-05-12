<?php


/**
 * TODO
 * Firebase Cloud Messaging API (V1)
 * Json file from services account
 * https://console.firebase.google.com/
 */
$firebaseAuthFile = "android-sms-gateway-7c8a7-firebase-adminsdk-bdr9l-930c0dc510.json";
$firebaseProject = "android-sms-gateway-7c8a7";

// RECEIVING MESSAGE
$number = urldecode($_POST['number']);
$message = urldecode($_POST['message']);
// type = received / sent / delivered / USSD
$type = urldecode($_POST['type']);

if (!empty($number) && !empty($message) && !empty($type)) {
    // Process received SMS in here
    // $type sent = success / Generic failure / No service / Null PDU / Radio off
    // $type delivered = success / failed
    die('DONE');
}


// SENDING MESSAGE

$to = urldecode($_REQUEST['to']);
$text = urldecode($_REQUEST['text']);
$secret = urldecode($_REQUEST['secret']);
$token = urldecode($_REQUEST['deviceID']);
$sim = urldecode($_REQUEST['sim']);
//Time required if you use MD5
$time = $_REQUEST['time'];


if (isset($_GET['debug']) && count($_REQUEST) > 1)
    file_put_contents("log.txt", json_encode($_REQUEST) . "\n\n", FILE_APPEND);

if (empty($to) || empty($text) || empty($secret) || empty($token)) {
    readfile("info.txt");
    die();
}

$result = sendPush($token, $secret, $time, $to, $text, $sim);

if (isset($_GET['debug']) && count($_REQUEST) > 1)
    file_put_contents("log.txt", $result . "\n\n", FILE_APPEND);
echo $result;

function sendPush($token, $secret, $time, $to, $message, $sim = 0)
{
    global $firebaseProject;
    $url = "https://fcm.googleapis.com/v1/projects/$firebaseProject/messages:send";

    $fields = array(
        "message" => array(
            "token" => $token,
            'data' => array(
                "to" => $to,
                "time" => $time,
                "secret" => $secret,
                "message" => $message,
                "sim" => $sim,
            )
        )
    );

    $headers = array(
        'Authorization: Bearer ' . getToken(),
        'Content-Type: application/json'
    );

    $ch = curl_init();
    curl_setopt($ch, CURLOPT_URL, $url);
    curl_setopt($ch, CURLOPT_POST, true);
    curl_setopt($ch, CURLOPT_HTTPHEADER, $headers);
    curl_setopt($ch, CURLOPT_RETURNTRANSFER, true);
    curl_setopt($ch, CURLOPT_POSTFIELDS, json_encode($fields));

    $result = curl_exec($ch);

    curl_close($ch);

    return $result;
}

function base64UrlEncode($text)
{
    return str_replace(
        ['+', '/', '='],
        ['-', '_', ''],
        base64_encode($text)
    );
}

function getToken()
{
    global $firebaseAuthFile;
    $md5 = md5($firebaseAuthFile);
    // if exists and not expired
    if (file_exists("$md5.token") && time() - filemtime("$md5.token") < 3500) {
        return file_get_contents("$md5.token");
    }
    $authConfigString = file_get_contents($firebaseAuthFile);
    $authConfig = json_decode($authConfigString);
    // Read private key from service account details
    $secret = openssl_get_privatekey($authConfig->private_key);

    // Create the token header
    $header = json_encode([
        'typ' => 'JWT',
        'alg' => 'RS256'
    ]);
    // Get seconds since 1 January 1970
    $time = time();
    // Allow 1 minute time deviation between client en server (not sure if this is necessary)
    $start = $time - 60;
    $end = $start + 3600;
    // Create payload
    $payload = json_encode([
        "iss" => $authConfig->client_email,
        "scope" => "https://www.googleapis.com/auth/firebase.messaging",
        "aud" => "https://oauth2.googleapis.com/token",
        "exp" => $end,
        "iat" => $start
    ]);
    // Encode Header
    $base64UrlHeader = base64UrlEncode($header);
    // Encode Payload
    $base64UrlPayload = base64UrlEncode($payload);
    // Create Signature Hash
    openssl_sign($base64UrlHeader . "." . $base64UrlPayload, $signature, $secret, OPENSSL_ALGO_SHA256);
    // Encode Signature to Base64Url String
    $base64UrlSignature = base64UrlEncode($signature);
    // Create JWT
    $jwt = $base64UrlHeader . "." . $base64UrlPayload . "." . $base64UrlSignature;

    //-----Request token, with an http post request------
    $options = array('http' => array(
        'method'  => 'POST',
        'content' => 'grant_type=urn:ietf:params:oauth:grant-type:jwt-bearer&assertion=' . $jwt,
        'header'  => "Content-Type: application/x-www-form-urlencoded"
    ));
    $context  = stream_context_create($options);
    $responseText = file_get_contents("https://oauth2.googleapis.com/token", false, $context);
    $response = json_decode($responseText, true);
    file_put_contents("$md5.token", $response['access_token']);
    return $response['access_token'];
}
