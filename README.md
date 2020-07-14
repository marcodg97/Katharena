# KATHARENA
Katharena is a CLI for the creation and management of Netkit and Kathará laboratories.
It's possible to create and manage laboratories directly from the interface, or through files with a syntax derived from the ["Tacatà" project by Damiano Massarelli and Mariano Scazzariello](https://github.com/damiano-massarelli/Tacata)
Katharena is written in Java 8 and no type of library was used*

### WARNING!
Katharena is in no way associated with Katharà, I'm not trying to support all the Kathará features and i do not guarantee the correct operation and full compatibility with the latter.

### Features
- Very lightweight (only 40KB)
- Possibility to create and manage laboratories directly from the interface, or through files
- Ability to manage multiple laboratories simultaneously (by `switchto` command)
- Checking for any errors made by the user**
- IPv4 and IPv6 managed network interfaces 
- Tunnelling
- Advanced BGP configurations
- Support Client PC, Web server (with autogenerated index.html file), DNS, loopback interfaces etc.

### How to use
launching Katharena with the command `java -jar katharena.jar` will create an empty lab in memory named "lab", you can also use these two args:
1. `-l` or `--load`, launch Katharena and load in memory the labs contained in the specified file
2. `-n` or `--name`, launch Katharena and name the lab with the specified name

EXAMPLE: launch Katharena loading the lab contained in example.txt and naming it "testlab" launch Katharena loading the lab contained in example.txt and naming it "testlab"
```
java -jar katharena.jar -l example.txt --name testlab
```
Supported commands:
- `switchto`: create (if not present) and switch to the lab with the specified name
- `print`: print all the informations about the current lab
- `load`: load the lab contained in the specified file
- `save`: save the current lab in the specified file
- `make`: make the lab for Katharà, in the specified folder
- `wipe`: wipe the lab

### Syntax
All the configuration is based on function calls which affect the current device and/or the current interface unless otherwise specified. The current device and the current interface are those defined in the line in which the function is called.
For example, you can configure the router named "router" to have the interface eth0 connected at the link A with IP address 192.168.1.1, like this, in the CLI:
```
router router A 192.168.1.1|eth0
```
and like this, in the lab file:
```
$router(router, A, 192.168.1.1|eth0)
```


### Examples
You can find various examples in the folder "examples", all of those are Intermediate test of the "Infrastructures of Computer Networks" course at the Roma Tre University, more information [here](https://www.netkit.org/exams.html)

### License
Katharena is released by MIT license, more information in LICENSE file



*: Why? Well, why not? :D
**: Despite this, I cannot exclude the possibility of errors not detected by Katharena