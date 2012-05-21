<?php
//ini_set('display_errors', 'On');
require_once($_SERVER['DOCUMENT_ROOT'] . '/settings.php');
require_once($_SERVER['DOCUMENT_ROOT'] . $site_prefix . 'lib/recaptchalib.php');
require_once($_SERVER['DOCUMENT_ROOT'] . $site_prefix . 'lib/JSON.php');

$kMaxSz = 1000000;
$privatekey = "6LdqrtESAAAAAJd9UDNw9fU-48jojyoIaxp2XUbu";
$enableCaptcha = true;

function post_data($url, $params) {
  $curlOb = curl_init();
  curl_setopt($curlOb, CURLOPT_URL, $url);
  curl_setopt($curlOb, CURLOPT_POST, 1);
  curl_setopt($curlOb, CURLOPT_RETURNTRANSFER, 1);
  $contents = '';
  foreach ($params as $key => $value) {
    $contents = $contents . urlencode($key) . '=' . urlencode($value) . '&';
  }
  curl_setopt($curlOb, CURLOPT_POSTFIELDS, $contents);

  $output = curl_exec($curlOb);
  curl_close($curlOb);
  return $output;
}

function process_req($req) {
  global $kMaxSz, $site_prefix, $privatekey, $enableCaptcha;
  $resp = recaptcha_check_answer ($privatekey,
                                  $_SERVER["REMOTE_ADDR"],
                                  $_POST["recaptcha_challenge_field"],
                                  $_POST["recaptcha_response_field"]);
  if ($enableCaptcha && !$resp->is_valid) {
    return "That pesky Captcha didn't like your answer.  Be a good sport and try again.";
  }

  // validate email
  if (!isset($req['email'])) {
    return 'Please fill out the email field';
  }
  $email_str = $req['email'];
  if (!filter_var($req['email'], FILTER_VALIDATE_EMAIL)) {
    return 'Invalid email: ' . $req['email'];
  }

  $use_comp = false;
  if (isset($req['comp']) && $req['comp']) {
    $use_comp = true;
  }

  if (!isset($_FILES['documents'])) {
    return 'Corrupt form';
  }

  $files = array();
  foreach (array(0,1,2,3,4,5) as $idx) {
    if (!isset($_FILES['documents']['name'][$idx])) {
      continue;
    }
    if ($_FILES['documents']['size'][$idx] < 1) {
      continue;
    }

    if ($_FILES['documents']['size'][$idx] > $kMaxSz) {
      // too big, or not uploaded
      return 'File too large';
    }

    $file_name = str_replace(' ', '', $_FILES['documents']['name'][$idx]) . '_' . uniqid() . '.xml';
    $uploadFile = $_SERVER['DOCUMENT_ROOT'] . $site_prefix . 'uploads/' . $file_name;

    if (!move_uploaded_file($_FILES['documents']['tmp_name'][$idx], $uploadFile)) {
      // moving the file failed somehow
      return 'Failed to move file for processing';
    }

    $files[] = $file_name;
  }

  if (count($files) < 1) {
    return 'Please select at least one file to upload';
  }

  $request_id = uniqid();
  // so now we're sure we have at least one file
  if ($use_comp && count($files) > 1) {
    $upload_dir = $_SERVER['DOCUMENT_ROOT'] . $site_prefix . 'uploads/';
    $zip_file = uniqid() . '.zip';
    $file_list = implode($files, ' ');
    $cmd_str = "cd $upload_dir && zip $zip_file $file_list";
    shell_exec($cmd_str);
    $params = array(
        'email' => $email_str,
        'token' => $request_id,
        'url' => 'http://' . $_SERVER['SERVER_ADDR'] . $site_prefix . 'uploads/' . $zip_file
        );
      $json_str = post_data('http://leovip032.ncsa.uiuc.edu:8888/computeSimilarities', $params);
      $resp = json_decode($json_str);
      if ($resp->status->code != 0) {
        return $resp->status->message;
      }
  } else {
    $json = new Services_JSON();
    foreach ($files as $file) {
      $params = array(
        'email' => $email_str,
        'token' => $request_id,
        'url' => 'http://' . $_SERVER['SERVER_ADDR'] . $site_prefix . 'uploads/' . $file
      );
      $json_str = post_data('http://leovip032.ncsa.uiuc.edu:8888/submitDocument', $params);
      $resp = json_decode($json_str);
      if ($resp->status->code != 0) {
        return $resp->status->message;
      }
    }
  }

  return "Documents submitted successfully.  You will receive an email shortly.";
}

?>
