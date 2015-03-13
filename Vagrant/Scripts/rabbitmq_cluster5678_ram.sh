#!/usr/bin/env bash

cat >> /etc/apt/sources.list <<EOT
deb http://www.rabbitmq.com/debian/ testing main
EOT

cat /vagrant/hosts >> /etc/hosts

wget http://www.rabbitmq.com/rabbitmq-signing-key-public.asc
apt-key add rabbitmq-signing-key-public.asc

apt-get update

apt-get install -q -y screen htop vim curl wget
apt-get install -q -y rabbitmq-server

# RabbitMQ Plugins
echo "Configuring MQ Plugins"
# service rabbitmq-server stop
rabbitmq-plugins enable rabbitmq_management
rabbitmq-plugins enable rabbitmq_jsonrpc
# service rabbitmq-server start

rabbitmq-plugins list

# add admin user to use web interface for managing rabbitmq
echo "Creating ADMIN user"
rabbitmqctl add_user admin admin
rabbitmqctl set_user_tags admin administrator
rabbitmqctl list_users
rabbitmqctl set_permissions -p / admin ".*" ".*" ".*"

# restart for plugin to be effective
service rabbitmq-server stop
service rabbitmq-server start

# Cluster node
echo "CONFIG to join cluster rabbit@local-rabbitmq05..."
rabbitmqctl stop_app
rabbitmqctl reset
rabbitmqctl stop
cp /vagrant/.erlang.cookie.mq05 /var/lib/rabbitmq/.erlang.cookie
chown rabbitmq:rabbitmq /var/lib/rabbitmq/.erlang.cookie
rm -rf /var/lib/rabbitmq/mnesia
rabbitmq-server -detached
rabbitmqctl stop_app
rabbitmqctl join_cluster --ram rabbit@local-rabbitmq05
rabbitmqctl start_app

echo "DONE"
