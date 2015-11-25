#1/bin/bash
java -jar Botsquared.jar &
MyPID=$!
echo $MyPID
echo "kill $MyPID" > botstop.sh