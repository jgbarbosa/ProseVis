<?php
require_once($_SERVER['DOCUMENT_ROOT'] . '/ProseVis/settings.php');
require_once($site_root . '/lib/uploads.php');

if ($_SERVER['REQUEST_METHOD'] == 'POST') {
  $error_msg = process_req($_POST['documents']);
}
$content = $site_root . '/fragments/data.php';

include($site_root . '/fragments/base.php');
?>
