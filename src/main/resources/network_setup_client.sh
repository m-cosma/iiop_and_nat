#!/bin/bash

echo "Current Setup"
echo "============="
echo "      Server IP: $SERVER_IP"
echo "     GATEWAY_IP: $GATEWAY_IP"
echo

ip route add $SERVER_IP via $GATEWAY_IP
