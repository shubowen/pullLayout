cd pulllayout-library/

echo $BINTRAY_USER
echo $BINTRAY_API_KEY

gradle clean build bintrayUpload  -PbintrayUser=$BINTRAY_USER -PbintrayKey=$BINTRAY_API_KEY -PdryRun=false