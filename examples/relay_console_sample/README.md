XBee Relay Console Sample Application
=====================================

This application demonstrates the usage of the XBee Library for Android in order
to communicate with XBee devices over Bluetooth Low Energy and send data to
other XBee interfaces.

The example scans for Bluetooth devices and allows you to connect to your
XBee device. Once connected, you can use the User Data Relay console in order
to send and receive data to/from other interfaces of your module.

Demo requirements
-----------------

To run this example you need:

* One XBee3 module in API mode and its corresponding carrier board (XBIB or
  equivalent).
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

If the connection is successful, the Relay Console page appears. It shows a
list, initially empty, with the received User Data Relay messages and a button
to send new messages. To test the communication, follow these steps:

1. Open XCTU and add your module to it.
2. Once the module is added, change to the **Consoles** working mode and open
   the serial connection.
3. Create and add a frame using the **Frames Generator** tool with the following
   parameters:
   - Frame type:                             0x2D - User Data Relay
   - Frame ID:                               00
   - Dest. interface:                        Bluetooth [01]
   - RF data (ASCII):                        Hello XBee!
4. Send this frame by selecting it and clicking the **Send selected Frame**
   button.

When the User Data Relay message is sent, verify that a line with the source
interface and data appears on the list of the mobile application.

To test the other way of the communication, follow these steps:

1. In the mobile application, tap on the **Send User Data Relay** button.
2. Select **SERIAL** as destination interface and enter any message.
3. Tap on **Send**.

When the User Data Relay message is sent, verify that a new **User Data Relay
Output** frame is received in the XCTU console.

Compatible with
---------------

* Android devices
* Digi Embedded devices

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