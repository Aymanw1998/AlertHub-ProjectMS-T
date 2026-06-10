@echo off
echo ==========================================
echo Starting Kafka (KRaft Mode)...
echo ==========================================
cd C:\kafka
bin\windows\kafka-server-start.bat config\server.properties
pause