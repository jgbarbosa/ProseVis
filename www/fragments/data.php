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
<input type="hidden" name="MAX_FILE_SIZE" value="1000000" />
<div style="margin-bottom:1em;">
Email: <input type="text" name="documents[email]" />
</div>

<div style="margin-bottom:1em;">
<p>Users may upload up to 6 documents to be processed at a time.  Optionally, we can compute comparisons between the documents, although this will take longer than processing the documents without comparisons.</p>

Document 1: <input type="file" name="documents[]" /><br />
Document 2: <input type="file" name="documents[]" /><br />
Document 3: <input type="file" name="documents[]" /><br />
Document 4: <input type="file" name="documents[]" /><br />
Document 5: <input type="file" name="documents[]" /><br />
Document 6: <input type="file" name="documents[]" /><br />
<br />
Compute comparisons? <input type="checkbox" name="documents[comp]" /><br /><br />
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
