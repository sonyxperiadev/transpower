# TransPower

## About

### Origin
(Skip ahead if you know about SAR.)

FCC and CELEC regulate allowed levels of SAR (Specific Absorption Rate) values of
radiation emitted by a mobile device. Common approach is to reduce power used by
modem in certain use cases so that less radiation is emitted through device's
antennas.

### Specific approach
The immediate goal of this project is to have an open source implementation ready
for *Open Devices* program with M-MR1 and N-MR1 versions of AOSP software running
on selected devices. The initial implementation focuses on an application which
transmits a message to a power module about the current status of device in
order to initiate change in power.

### Future goals
Since AOSP runs on multiple platforms, the end goal of this project is to run on
any Android version and physical platform used by Sony Mobile Communications Open
Devices program and AOSP.

## Modules

### apps
In order to build specific APK for your need you can run

    make <module_name>

but also you can put the particular module name under PRODUCT_PACKAGES
in your device's make file if you want it automatically installed on your
device when you flash device images.

##### * TransPowerBase
Basic power transmission based on battery, WiFi and telephony.

##### * TransPowerAcc
Together with power transmission based on TransPowerBase module it also sends
state of accelerometer to the modem.

##### * TransPowerProx
Together with power transmission based on TransPowerBase module it also sends
state of proximity sensor to the modem.

##### * TransPowerSensors
Together with power transmission based on TransPowerBase module it also sends
states of accelerometer and proximity sensors to the modem as well as stops
listening to accelerometer changes if proximity is detected.

### libraries

Apps described above depend on the following libraries:
##### * common
Contains definitions of all the common functions of the app taking care of the
app's life cycle as well as shared functionalities such as monitoring battery,
wifi and telephony.

##### * common-sensor
Contains definitions of common functions of sensor observers.

##### * libacc
Implements accelerometer functionality.

##### * libpower
Implements communication protocol using OEMHOOK API, by which modem is
informed about environment states, such as network or sensors, so that
it can make a decision on whether to reduce the power or not.

##### * libprox
Implements observation of proximity sensor.

### app versioning
Beginning with versions 1.1.0 and 2.1.0, apps will follow semantic versioning scheme.
- *Version name* x.y.z describes the following: x = major build, y = minor build and z = patch.
- *Version code* is an integer created out of x.y.z by:
  - ((n & xF0000000) >> 28) + 1 = x
  - (n & x0FFF0000) >> 16 = y
  - n & x0000FFFF = z

## LICENSE
The project is licensed under the license stated in the LICENSE file of this project.

## AUTHORS
The original feature was implemented by the Sony Mobile Communications telephony
team. All the contributors are duly noted in the AUTHORS file of this project.
