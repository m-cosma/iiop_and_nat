#!/bin/bash

echo "Current Setup"
echo "============="
echo "     Server IP: $SERVER_IP"
echo "   Server Port: $SERVER_PORT"
echo "Starting Corba client..."
echo
java -cp /app/iiop-and-nat-1.0.jar:/app/lib/* -Dlog4j.configurationFile=/app/log4j2.xml com.rsi.example.client.IiopNatClient $SERVER_IP $SERVER_PORT
