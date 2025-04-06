#!/bin/bash

echo "Current Setup"
echo "============="
echo "   Listening Port: $SERVER_PORT"
echo "   IOR Proxy Host: $IOR_PROXY_HOST"
echo "Starting Corba server..."
echo
java -cp /app/iiop-and-nat-1.0.jar:/app/lib/* -Dlog4j.configurationFile=/app/log4j2.xml com.rsi.example.server.IiopNatServer $SERVER_PORT $IOR_PROXY_HOST
