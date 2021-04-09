# XBee Library for Android [![Maven Central](https://maven-badges.herokuapp.com/maven-central/com.digi.xbee/xbee-android-library/badge.svg)](https://maven-badges.herokuapp.com/maven-central/com.digi.xbee/xbee-android-library)

This project contains the source code of the XBee Library for Android, an
easy-to-use API built on top of the
[XBee Java Library](https://github.com/digidotcom/xbee-java).

The XBee Library for Android allows you to create Android applications that
interact with Digi International's [XBee](http://www.digi.com/xbee/) radio
frequency (RF) modules. The connection with XBee modules can be wireless
(through the Bluetooth Low Energy interface) or serial (through the USB host
serial port or Digi's serial port). In either case, the library facilitates the
development of Android applications that interact with XBee modules.

The project includes the source code and examples that show how to create
Android applications that communicate with XBee devices using the available
APIs. The examples are also available in source code format. The source of the
project has been contributed by [Digi International](http://www.digi.com).

The main features of the library include:

* Support for ZigBee, 802.15.4, DigiMesh, Point-to-Multipoint, Wi-Fi, Cellular
  and Thread XBee devices.
* Support for API and API escaped operating modes.
* Support for different communication interfaces with the XBee device. These
  include:
  * Bluetooth Low Energy.
  * USB host.
  * Digi's serial port.
* Management of local (attached to the Android device) and remote XBee device
  objects.
* Discovery of remote XBee devices associated with the same network as the
  local device.
* Configuration of local and remote XBee devices:
  * Configure common parameters with specific setters and getters.
  * Configure any other parameter with generic methods.
  * Execute AT commands.
  * Apply configuration changes.
  * Write configuration changes.
  * Reset the device.
* Transmission of data to all the XBee devices on the network or to a specific
  device.
* Reception of data from remote XBee devices:
  * Data polling.
  * Data reception callback.
* Transmission and reception of IP, IPv6, CoAP and SMS messages.
* Reception of network status changes related to the local XBee device.
* IO lines management:
  * Configure IO lines.
  * Set IO line value.
  * Read IO line value.
  * Receive IO data samples from any remote XBee device on the network.
* Support for explicit frames and application layer fields (Source endpoint,
  Destination endpoint, Profile ID, and Cluster ID).
* Support for User Data Relay frames, allowing the communication between
  different interfaces (Serial, Bluetooth Low Energy and MicroPython).


## Start Here

As the XBee Library for Android is a layer of the Java one, it is documented as
part of the XBee Java Library. So, the best place to get started is the
[XBee Java Library documentation](http://www.digi.com/resources/documentation/digidocs/90001438/#reference/r_xb_java_lib_android.htm).


## How to Contribute

The contributing guidelines are in the
[CONTRIBUTING.md](https://github.com/digidotcom/xbee-android/blob/master/CONTRIBUTING.md)
document.


## License

Copyright 2019-2021, Digi International Inc.

This Source Code Form is subject to the terms of the Mozilla Public
License, v. 2.0. If a copy of the MPL was not distributed with this
file, you can obtain one at http://mozilla.org/MPL/2.0/.

THE SOFTWARE IS PROVIDED "AS IS" AND THE AUTHOR DISCLAIMS ALL WARRANTIES
WITH REGARD TO THIS SOFTWARE INCLUDING ALL IMPLIED WARRANTIES OF
MERCHANTABILITY AND FITNESS. IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR
ANY SPECIAL, DIRECT, INDIRECT, OR CONSEQUENTIAL DAMAGES OR ANY DAMAGES
WHATSOEVER RESULTING FROM LOSS OF USE, DATA OR PROFITS, WHETHER IN AN
ACTION OF CONTRACT, NEGLIGENCE OR OTHER TORTIOUS ACTION, ARISING OUT OF
OR IN CONNECTION WITH THE USE OR PERFORMANCE OF THIS SOFTWARE.