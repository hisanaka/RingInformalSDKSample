Ring Informal SDK Sample
===
# Overview

This project has contained the sample Android application that uses the Ring Informal SDK.

The Ring Informal SDK is fan-made Android library to operate Ring / Ring ZERO at your Android application.

The Ring Informal SDK is *NOT* related with logbar Inc.

# Description

"Ring / Ring ZERO" is the ring-type wearable device that has been developed by logbar Inc. See more details, <http://logbar.jp/ring>

At the time January 1, 2015, SDK for Ring was not published. Therefore, I began to challenge in the development of SDK.

In this project, the sample Android application that use Ring Informal SDK has been included.

Currently, the following functions have been implemented in the Ring Informal SDK.

- Gesture detection.
- Gesture registration.
- Obtain the Ring device information. e.g., Serial number, Model number, etc.
- Send notification(LED and/or vibration).

The Ring Informal SDK itself, ringinformaldriver-release.aar, is stored in the "ringinformaldriver" directory.

# Demo

<http://www.youtube.com/watch?v=ngiRk_TddcQ>

# Exemption from responsibility

This project and Ring Informal Driver is **NOT** related with logbar Inc. Therefore, there is **no warranty of any kind and support by logbar Inc.**

The technology used in the Ring Informal SDK is based on the information that I have analyzed, not has been published by logbar Inc. So, there is no warranty of any kind.

That is, **"All at your own risk".**

# Requirement

- logbar Ring or Ring ZERO

- Android device that support BLE with Android 4.4(KitKat) or later.

- Android app development environment. (Android Studio recommend.)

- Motivation and ideas, besides a little bit of recklessness.

# Usage

## <a name ="basis">Basis

1. Incorporate the Ring Informal SDK to you application.

1. Implements RingDeviceCallback.

        RingDevice.RingDeviceCallback mRingDeviceCallback = new RingDevice.RingDeviceCallback() {

          @Override
          public void onDeviceConnected(){
            /* Do something when the connection is successful. */
          };

          @Override
          public void onDeviceConnectionFailed(int result) {
            /* Do something when the connection is failed.
             * result indicates the reason for the failure.
             * 0 : Bluetooth / BLE is not available.
             * 1 : Ring can not be found.
             * 2 : BLE service can not be found.
             * 3 : BEL characteristic can not be found.
             * 4 : Connection is congested. Reconnect after waiting for a while.
             */
          };

          @Override
          public void onDeviceDisconnected() {
            /* Do something after the connection has been cut. */
          };

          @Override
          public void onCharacteristicRead(String name, String value) {
            /* Do something when BLE characteristic is read.
             * "name" is characteristic name. See RingDeviceInformation class.
             * "value" is characteristic value.
             */
          };

          @Override
          public void onCharacteristicWrote(int result) {
            /* Do something when notification is done. */
          };

          @Override
          public void onGestureDetected(GestureInformation perform, ArrayList<GestureInformation> recognize) {
            /* Perform action against the gesture.
             * "perform" is the gesture information of performing,
             * "recognized" is some of the gesture information that recognized.
             */
          };

          @Override
          public void onGestureRegistered(ArrayList<GestureInformation> gestures) {
            /* Register the action against the gesture.
             * "gestures" is the information of gesture.
             * Currently, the size of list is one.
             * In the future, there is possibility that allows
             * the registration from files, it has adopted the array list.
             */
          };
        }

1. Create the instance of RingDevice.

        RingDevice myRing = new RingDevice(getContext(), mRingDeviceCallback);

1. To connect Ring, call `RingDevice.connect()` method.

\[ NOTE \]

When connected to Ring, you may need to long press the touch sensor. Therefore, it is recommended that show a dialog to guide the touching the touch sensor until `RingDeviceCallback.onDeviceConnected()` method or `RingDeviceCallback.onDeviceConnectionFailed` method is invoked.

## Gesture registration

In the Ring Informal SDK, the gesture to be detected is registered by performing actually.

1. Connect to the Ring. See [Basis](#basis).

1. To initiate the registration, call `RingDevice.registerGetsure(true)` method.

1. Perform the gesture.

1. Invoke `RingDeviceCallback.onGestureRegistered()` method. In this method, you will get the gesture ID from the argument and then register the action that you want to run against the ID.

1. To terminate the registration, call `RingDeivce.registerGestureRegistration(false)`.

\[**Important** \]

If you want to cancel the registration of the performed gesture, call `RingDevice.removeGetsure(id)` method to delete the registered gesture information in the internal database of the Ring Informal SDK.

## Gesture detection

When successfully connected to Ring, it will be in the gesture waiting state.

1. Connect to the Ring. See [Basis](#basis).

1. Perform the gesture.

1. Invoke `RingDeviceCallback.onGestureDetected()` method. In this method, you will get the gesture ID from second argument, and then perform the action that has been registered against the ID.

\[ NOTE \]

The second argument of `RingDeviceCallback.onGestureDetected()` method is an array list of gesture information that the difference rate is less than or equal to the threshold. This array list is sorted in ascending order of the difference rate, not ID.

If there is no gesture that the rate of difference is less than or equal to the threshold, the second argument size is 0.

If you want to change the threshold, call `RingDevice.setThreshold()` method. The default threshold is 10.0%.

# Gesture Information

The information of gesture is represented as `GestureInformation` class.

`GestureInformation` class has the following methods.

| Method                   | action                                 |
|:-------------------------|:---------------------------------------|
| long getId()             | Get the gesture ID.                    |
| Bitmap getBitmap()       | Get Bitmap that represent the gesture. |
| float getDifferentRate() | Get the difference rate between performed gesture and recognized one. |

# For improvement

Honestly, the gesture recognition accuracy of the Ring Informal SDK is not high enough.

In addition, the usability of the Ring Informal SDK may not be good.

Therefore, if you have a good idea for the image recognition, and/or you have a positive opinion on this project, please contact me.

# Thanks

logbar Inc. that developed this "wonderful" device.
