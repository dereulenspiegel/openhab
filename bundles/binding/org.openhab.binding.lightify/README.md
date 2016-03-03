# Lightify binding

This binding allows openHAB to connect to a lightify gateway to control connected
luminaries (lights, groups etc.).

## Configuration

```
lightify.host=192.168.0.42  # The IP or hostname of the lightify gateway
lightify.transition=100     # The time to take for transitioning between two states (like dimming)
```

## Item Configuration

Only Dimmer-, Switch-, and ColorItems can be bound to a lightify configuration. To do this you
need to specify either the device address or the device name you want to control and the type of
value you want to control. Optionally you can specify a time duration the luminary can take for its
transition. Addresses are specified as colon delimited hexadecimal bytes. For groups a single hexadecimal
represented byte without any colons is expected. Lights etc. are expected to have an u byte address.
Known devices and their addresses are written with info level to the log during startup.

Controllable values are 
* SWITCH
* COLOR
* TEMPERATURE
* LUMINANCE

The configuration takes the form of

```
{lightify="<name or address>;<controllable value>[;<transition duration>]"}
```

## Examples

```
# Dimming a luminary

Dimmer LivingroomLightDimmer {lightify="Livingroom1;LUMINANCE"}

# Controlling temperature of a light

Dimmer LivingroomLightTemperature {lightify="livingroom1;TEMPERATURE"}
```