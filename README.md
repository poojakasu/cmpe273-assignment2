cmpe273-assignment2
===================

CMPE273 Assignment 2.

Uses Apollo Queue broker: Master (54.215.210.214)

nohup ./bin/run.sh 0<&- &> /tmp/app_procurementser.log &

nohup ./bin/library_b.sh 0<&- &> /tmp/app_library_B.log &

nohup ./bin/library_a.sh 0<&- &> /tmp/app_library_A.log &

ps -elf | grep java
tail -f /tmp/app_procurementser.log
tail -f /tmp/app_library_B.log
tail -f /tmp/app_library_A.log

Note: If connection to queue is stopped, Please refresh the page
