#!/usr/bin/env bash

cat >> /etc/apt/sources.list <<EOT
deb http://www.rabbitmq.com/debian/ testing main
EOT

wget http://www.rabbitmq.com/rabbitmq-signing-key-public.asc
apt-key add rabbitmq-signing-key-public.asc

apt-get update

apt-get install -q -y screen htop vim curl wget
apt-get install -q -y rabbitmq-server

# RabbitMQ Plugins
# service rabbitmq-server stop
rabbitmq-plugins enable rabbitmq_management
rabbitmq-plugins enable rabbitmq_jsonrpc
# service rabbitmq-server start

rabbitmq-plugins list

# add admin user to use web interface for managing rabbitmq
rabbitmqctl add_user admin admin
rabbitmqctl set_user_tags admin administrator
rabbitmqctl list_users
rabbitmqctl set_permissions -p / admin ".*" ".*" ".*"

# restart for plugin to be effective
service rabbitmq-server stop
service rabbitmq-server start

echo "DONE"
