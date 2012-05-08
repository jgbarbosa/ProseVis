#!/bin/bash

mkdir -p executables/ProseVis.app
mkdir -p executables/ProseVis.app/Contents
mkdir -p executables/ProseVis.app/Contents/MacOS
mkdir -p executables/ProseVis.app/Contents/Resources/Java

cp resources/prosevis.png executables/ProseVis.app/
cp executables/prosevis.jar lib/processingopengl/macosx/*.jnilib lib/processingopengl/*.jar executables/ProseVis.app/Contents/Resources/Java/
MY_VAR="hello world"
cat > executables/ProseVis.app/Contents/Info.plist << '</plist>'
<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE plist PUBLIC "-//Apple//DTD PLIST 1.0//EN" "http://www.apple.com/DTDs/PropertyList-1.0.dtd">
<plist version="1.0">
<dict>
  <key>CFBundleExecutable</key>
  <string>run.sh</string>
  <key>CFBundleIconFile</key>
  <string>prosevis.png</string>
  <key>CFBundleInfoDictionaryVersion</key>
  <string>1.0</string>
  <key>CFBundlePackageType</key>
  <string>APPL</string>
  <key>CFBundleSignature</key>
  <string>????</string>
  <key>CFBundleVersion</key>
  <string>1.0</string>
  <key>Java</key>
    <dict>
      <key>JVMVersion</key>
      <string>1.6+</string>
      <key>MainClass</key>
      <string>prosevis.EntryPoint</string>
      <key>Properties</key>
        <dict>
          <key>apple.laf.useScreenMenuBar</key>
          <string>true</string>
        </dict>
      <key>VMOptions</key>
      <string>-Xmx1024m</string>
      <key>WorkingDirectory</key>
      <string>$USER_HOME</string>
      <key>ClassPath</key>
      <string>$MY_VAR</string>
    </dict>
</dict>
</plist>

cat > executables/ProseVis.app/Contents/MacOS/run.sh << 'exit'
#!/bin/sh
exec /System/Library/Frameworks/JavaVM.framework/Resources/MacOS/JavaApplicationStub "$@"
exit
