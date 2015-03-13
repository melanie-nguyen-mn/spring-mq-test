#!/usr/bin/env bash

cat >> /etc/apt/sources.list <<EOT
deb http://www.rabbitmq.com/debian/ testing main
EOT

cat >> /etc/hosts <<EOT
192.168.50.54   local-rabbitmq05
192.168.50.64   local-rabbitmq06
192.168.50.74   local-rabbitmq07
192.168.50.84   local-rabbitmq08
EOT

wget http://www.rabbitmq.com/rabbitmq-signing-key-public.asc
apt-key add rabbitmq-signing-key-public.asc

apt-get update

apt-get install -q -y screen htop vim curl wget
apt-get install -q -y rabbitmq-server

# RabbitMQ Plugins
echo "Configuring PLUGINs"
# service rabbitmq-server stop
rabbitmq-plugins enable rabbitmq_management
rabbitmq-plugins enable rabbitmq_jsonrpc
# service rabbitmq-server start

rabbitmq-plugins list

# restart for plugin to be effective
service rabbitmq-server stop
service rabbitmq-server start


# RabbitMQ cluster config
echo "Configuring CLUSTER"
rabbitmqctl stop_app
rabbitmqctl reset
rabbitmqctl stop

cp /vagrant/.erlang.cookie /var/lib/rabbitmq
cp /vagrant/rabbitmq.config.cluster5678 /etc/rabbitmq/rabbitmq.config
rm -rf /var/lib/rabbitmq/mnesia

rabbitmq-server -detached
rabbitmqctl stop_app
rabbitmqctl start_app

# add admin user to use web interface for managing rabbitmq
echo "Adding ADMIN user"
rabbitmqctl add_user admin admin
rabbitmqctl set_user_tags admin administrator
rabbitmqctl list_users
rabbitmqctl set_permissions -p / admin ".*" ".*" ".*"

echo "DONE"
