<div><p>
ProseVis displays documents after they have have been processed to extract meta-information about the sound of text.  ProseVis users may upload their documents for processing on this page.  Processing may take anywhere from five minutes to a couple days depending on server load.  We will email you at your provided address with a link to download the processed data accepted by ProseVis.
</p></div>

<div><p>
Currently, the data processing machinery only accepts TEI-XML files.
</p></div>

<div><p style="color:red">
<?php
if (isset($error_msg)) {
  echo $error_msg;
}
?>
</p></div>

<form action="<?php echo $site_prefix; ?>/data/index.php" enctype="multipart/form-data" method="post">
<input type="hidden" name="MAX_FILE_SIZE" value="10000000" />
<div style="margin-bottom:1em;">
Email: <input type="text" name="documents[email]" />
</div>

<div style="margin-bottom:1em;">
<p>Users may upload a document or zip file containing documents for processing.  If a zip file is provided, comparisons will be calculated between the contained documents.</p>

Document or zip file: <input type="file" name="documents[]" />

<br>

</div>

<!-- Capcha stuff -->
<div style="margin-bottom:1em;">
<?php
  require_once($site_root . '/lib/recaptchalib.php');
  $publickey = "6LdqrtESAAAAAFVqiQ9BCphNdxVmxXO2KVKtImdD";
  echo recaptcha_get_html($publickey);
?>
</div>

<input type="submit" />

</form>
