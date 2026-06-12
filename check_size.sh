FILESIZE=$(stat -c%s "AnversePhone.apk")
echo "Size: $FILESIZE"
if (( FILESIZE > 26214400 )); then
    echo "Too big"
fi
