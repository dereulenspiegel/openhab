Junkers gas heater control
==========================
The burner in a Junkers gas heating system can be controlled via the "124" bus.
Normally a room thermostat is connected to these three wires, but in many cases such a room
thermostat is simply too stupid for smart homes.

Luckily the "124" bus is pretty simple. One wire is ground, one wire provides +24VDC and the third wire
is used to control the burner. If you provide a voltage between 0-24V to this wire you can set the burner
from 0% to 100%. This hardware lets you simply send commands to an arduino to create the necessary voltage.

All this is based on an [article from the FHEM community](http://www.fhemwiki.de/wiki/Junkers_Therme_Stetigregelung). But
instead of using an Ethersex board I use an ordinary Arduino Uno with a custom sketch.