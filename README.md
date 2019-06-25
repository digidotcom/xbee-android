XBee Library for Android
========================

This project contains the source code of the XBee Library for Android, an
easy-to-use API developed in Java that allows you to interact with Digi
International's [XBee](http://www.digi.com/xbee/) radio frequency (RF) modules.
This source has been contributed by [Digi International](http://www.digi.com).

The XBee library for Android is a layer of the
[XBee Java Library](https://github.com/digidotcom/XBeeJavaLibrary) and provides
support to connect and communicate with XBee modules in Android devices. The
connection with XBee modules can be wireless (through the Bluetooth Low Energy
interface) or serial (through the USB host serial port or Digi's serial port).
In either case, the library facilitates the development of Android applications
that interact with XBee modules.

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

Start Here
----------

As the XBee Library for Android is a layer of the Java one, it is documented as
part of the XBee Java Library. So, the best place to get started is the
[XBee Java Library documentation](http://www.digi.com/resources/documentation/digidocs/90001438/Default.htm).

How to Contribute
-----------------

The contributing guidelines are in the
[CONTRIBUTING.md](https://github.com/digidotcom/XBeeAndroidLibrary/blob/master/CONTRIBUTING.md)
document.

License
-------
Copyright 2019, Digi International Inc.

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