<?php
//ini_set('display_errors', 'On');
require_once($site_root . '/settings.php');
require_once($site_root . '/lib/recaptchalib.php');
require_once($site_root . '/lib/JSON.php');

$kMaxSz = 2 * 1000 * 1000; // less than 2MB
$privatekey = "6LdqrtESAAAAAJd9UDNw9fU-48jojyoIaxp2XUbu";
$enableCaptcha = true;
$doc_root = $_SERVER['DOCUMENT_ROOT'];


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

function rrmdir($dir) {
  foreach(glob($dir . '/*') as $file) {
    if(is_dir($file)) {
      rrmdir($file);
    } else {
      unlink($file);
    }
  }
  rmdir($dir);
}

function process_workspace($workspace, $use_comp, $email, $request_id) {
  global $doc_root, $kMaxSz;
  $files = array();
  foreach (glob($doc_root . $workspace . '*') as $file) {
    $suff = substr($file, -4);
    if ($suff != '.xml') {
      return 'Please submit only .xml files';
    }

    if (filesize($file) > $kMaxSz) {
      return "$file is too big for submission";
    }
    $old_name = substr($file, strlen($doc_root . $workspace));
    $safe_name = substr($old_name, 0, strlen($old_name) - 4);
    $safe_name = str_replace('.', '_', $safe_name);
    $safe_name = str_replace(' ', '_', $safe_name);
    $safe_name = $safe_name . '.xml';

    if ($old_name !== $safe_name) {
      if (!rename($file, $doc_root . $workspace . $safe_name)) {
        // moving the file failed somehow
        return 'Internal error: Failed to move file for processing';
      }
    }

    $files[] = $safe_name;
  }

  if (count($files) < 1) {
    return 'Please submit at least one document';
  }

  if ($use_comp === False) {
    foreach($files as $file) {
      $params = array(
        'email' => $email,
        'token' => $request_id,
        'url' => 'http://' . $_SERVER['SERVER_ADDR'] . $workspace . $file
      );
      $json_str = post_data('http://leovip032.ncsa.uiuc.edu:8888/submitDocument', $params);
      $resp = json_decode($json_str);
      if ($resp->status->code != 0) {
        return 'Remote request failed: ' . $resp->status->message;
      }
    }
  } else {
    $zip_file = uniqid() . '.zip';
    shell_exec("cd $doc_root$workspace  && zip $zip_file *");
    $params = array(
      'email' => $email,
      'token' => $request_id,
      'url' => 'http://' . $_SERVER['SERVER_ADDR'] . $workspace . $zip_file
    );

    $json_str = post_data('http://leovip032.ncsa.uiuc.edu:8888/computeSimilarities', $params);
    $resp = json_decode($json_str);
    if (!isset($resp->status->code) || $resp->status->code !== 0) {
      return 'Remote request failed: ' . $resp->status->message;
    }
    echo "<!-- $json_str -->";
  }
  return 'Documents submitted successfully.  You will receive an email shortly.';
}


function process_req($req) {
  global $doc_root, $site_prefix, $privatekey, $enableCaptcha, $kMaxSz;
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


  if (!isset($_FILES['documents'])) {
    return 'Corrupt form';
  }

  $workspace = $site_prefix . '/uploads/' . uniqid() . '/';
  mkdir($doc_root . $workspace);

  $ret_msg = 'OK';
  $use_comp = False;

  foreach (array(0) as $idx) {
    if (!isset($_FILES['documents']['name'][$idx])) {
      continue;
    }

    $file_name = $_FILES['documents']['name'][$idx];
    $suff = substr($file_name, -4);
    if ($suff === '.xml') {
      if (!move_uploaded_file($_FILES['documents']['tmp_name'][$idx], $doc_root . $workspace . $file_name)) {
        $ret_msg = 'Internal error: failed to move file';
        break;
      }
    } elseif ($suff === '.zip') {
      $orig_zip = $_FILES['documents']['tmp_name'][$idx];
      shell_exec("cd $doc_root$workspace && unzip \"$orig_zip\"");
      $use_comp = True;
    } else {
      $ret_msg = 'Please upload either an .xml or .zip file.';
      break;
    }
  }

  if ($ret_msg === 'OK') {
    $ret_msg = process_workspace($workspace, $use_comp, $email_str, uniqid());
  }

  rrmdir($doc_root . $workspace);

  return $ret_msg;
}


?>
