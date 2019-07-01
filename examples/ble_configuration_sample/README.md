XBee BLE Configuration Sample Application
=========================================

This application demonstrates the usage of the XBee Library for Android in order
to configure XBee devices over Bluetooth Low Energy.

The example scans for Bluetooth devices and allows you to connect to your
XBee device. Once connected, the application displays a small subset of the XBee
settings which you can read or write.

Demo requirements
-----------------

To run this example you need:

* One XBee3 module and its corresponding carrier board (XBIB or equivalent).
* An Android device.
* The XCTU application (available at www.digi.com/xctu).

Demo setup
----------

Make sure the hardware is set up correctly:

1. The Android device is powered on.
2. The XBee module is plugged into the XBee adapter and connected to your
   computer's USB or serial port.
3. The XBee module in in API mode and has the Bluetooth interface enabled and
   configured.

Demo run
--------

The example is already configured, so all you need to do is to build and launch
the project.

In the first page of the application you have to select your XBee device from
the list of Bluetooth devices. Tap on it and enter the Bluetooth password you
initially configured when enabled BLE in your device.

If the connection is successful, the Configuration page appears. It shows a
small subset of the XBee settings, some of them writable and others read-only.
To verify that the application can read and write the settings over Bluetooth,
tap the **Read** button to get their values. Then, change any of them and
tap the **Write** button. 

Compatible with
---------------

* Android devices

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