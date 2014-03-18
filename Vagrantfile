# -*- mode: ruby -*-
# vi: set ft=ruby :

# Vagrantfile API/syntax version. Don't touch unless you know what you're doing!
VAGRANTFILE_API_VERSION = "2"

Vagrant.configure(VAGRANTFILE_API_VERSION) do |config|
  config.vm.box = "hashicorp/precise32"

  config.vm.provision :shell, :path => "bootstrap.sh"

  config.vm.network :forwarded_port, host: 3000, guest: 3000 # connect
  config.vm.network "private_network", ip: "192.168.50.3"
end

