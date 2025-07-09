---
title: Modbus Steps
date: 2025-07-09
slug: /en/plugins/modbus
---


This plugin provides a set of steps to interact with devices and systems that use the Modbus protocol, a widely used 
industrial communication protocol for automation and process control.

Modbus is a serial communication protocol originally developed by Modicon (now Schneider Electric) in 1979 for use 
with programmable logic controllers (PLCs). Today, it is one of the most common protocols used in industry to 
connect electronic industrial devices due to its simplicity and robustness.

This plugin implements the Modbus TCP variant, which allows communication over TCP/IP networks, facilitating 
integration with modern systems and communication over standard Ethernet networks. With this plugin, you can:

- Establish connections with Modbus TCP devices
- Read register values from Modbus devices
- Write values to specific registers
- Verify read values to validate system behavior


---
## Table of contents

---


## Installation


Include the module in the corresponding section.

```text tabs=coord name=yaml copy=true
es.iti.wakamiti:modbus-wakamiti-plugin:1.0.0
```

```text tabs=coord name=maven copy=true
<dependency>
  <groupId>es.iti.wakamiti</groupId>
  <artifactId>modbus-wakamiti-plugin</artifactId>
  <version>1.0.0</version>
</dependency>
```


## Configuration

### `modbus.host`
- Type: `string`
- Default: `localhost`

Sets the IP address or hostname of the Modbus master device to which the plugin will connect. This parameter is 
essential for establishing communication with the Modbus TCP server.

Example:
```yaml
modbus:
  host: 172.17.0.1
```


### `modbus.port`
- Type: `integer`
- Default: `5020`

Sets the TCP port used for communication with the Modbus master device. The standard port for Modbus TCP is 502, 
although alternative ports are often used in many environments for security reasons or network configuration.

Example:
```yaml
modbus:
  port: 5021
```


### `modbus.slaveId`
- Type: `integer`
- Default: `1`

Sets the identifier of the Modbus slave device you want to communicate with. In a Modbus network, each slave device 
has a unique identifier that allows the master to direct commands specifically to that device.

Valid values for the slave ID are in the range of 1 to 247, with the value 1 commonly used for the first device in 
many configurations. In networks with multiple Modbus devices, it is essential to configure this parameter correctly 
to communicate with the desired device.

Example:
```yaml
modbus:
  slaveId: 11
```


## Steps

### Define base URL
```text copy=true
the base URL tcp://{host}:{port}
```
Sets the connection address and port for the Modbus master device. 
This step is equivalent to configuring the properties [`modbus.host`](#modbushost) and [`modbus.port`](#modbusport) 
simultaneously.

#### Parameters:
| Name   | Wakamiti type           | Description |
|--------|-------------------------|-------------|
| `host` | `word` *required*       | base host   |
| `port` | `integer` *required*    | base port   |

#### Examples:
```gherkin
Given the base URL tcp://example.org:5021
```


### Define slave ID
```text copy=true
the slaveId {id}
```
Sets the identifier of the Modbus slave device you want to communicate with. 
This step is equivalent to configuring the property [`modbus.slaveId`](#modbusslaveid).

#### Parameters:
| Name | Wakamiti type           | Description |
|------|-------------------------|-------------|
| `id` | `integer` *required*    | slave id    |

#### Examples:
```gherkin
Given the slaveId 11
```


### Read registers
```text copy=true
{quantity} (is|are) read from the address {address}
```
Reads a specific number of holding registers from a given address in the Modbus slave device. Holding registers are 
one of the most common data types in Modbus and are used to store values that can be both read and written.

This step executes the Modbus function 03 (Read Holding Registers) and internally stores the read values for later 
verification. Each register read is a 16-bit (2 bytes) value that can represent various data types depending on the 
device implementation.

Important considerations:
- The starting address must be valid for the specific device
- The requested quantity of registers must not exceed the device's capacity
- Registers are numbered from 0, although some device documentation may reference them from 1
- This step must be executed after correctly setting the slave ID

#### Parameters:
| Name       | Wakamiti type           | Description                       |
|------------|-------------------------|-----------------------------------|
| `quantity` | `integer` *required*    | Number of registers to read       |
| `address`  | `integer` *required*    | Starting address of the registers |

#### Examples:
```gherkin
When 5 are read from the address 100
```


### Write value
```text copy=true
the value {value} is written on the address {address}
```
Writes an integer value to a specific holding register in the Modbus slave device. This step allows you to modify 
the state or configuration of the remote Modbus device.

This step executes the Modbus function 06 (Write Single Register), which allows writing a single 16-bit value to a 
specific register address. It is one of the most common operations for controlling or configuring Modbus devices.

Important considerations:
- The value to write must be within the allowed range for a 16-bit register (0-65535)
- The register address must be valid and writable on the device
- Some devices may have read-only registers or specific restrictions
- This step must be executed after correctly setting the slave ID
- It is recommended to verify the written value with a subsequent read operation

#### Parameters:
| Name      | Wakamiti type           | Description                       |
|-----------|-------------------------|-----------------------------------|
| `value`   | `integer` *required*    | Value to write                    |
| `address` | `integer` *required*    | Position where to write the value |

#### Examples:
```gherkin
When the value 42 is written on the address 100
```


### Check read value
```text copy=true
registers read contains value {value}
```
Verifies that among the previously read registers (using the "Read registers" step) there is at least one that 
contains exactly the specified value. This step is essential for validating the expected behavior of the Modbus 
device and confirming that the register values are correct.

This step performs a search in the array of values obtained in the last read operation and generates an error if the 
specified value is not found in any of the read registers.

Important considerations:
- This step must be executed after a register read step
- If no previous read has been performed, an error will be generated
- The comparison is exact (the entire integer value must match)
- If you need to verify values at specific positions, it is recommended to perform individual reads for each position

#### Parameters:
| Name    | Wakamiti type           | Description                    |
|---------|-------------------------|--------------------------------|
| `value` | `integer` *required*    | Value to find in the registers |

#### Examples:
```gherkin
Then registers read contains value 42
```
