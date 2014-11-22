hesperbot-cracker
=================

Generates activation and uninstall codes using Hesperbot's Dalvik Executable.

Requirements
===========
This tool  uses the original malware `classes.dex` to generate the uninstall-codes by using Java Reflection.
It was successfully tested with this `classes.dex` file:

* MD5: `3d70ebfce0130c08772bf449d82d1235`
* SHA1: `8fce71267af12db8578c9676c58ecf6c2c3d0424`

If you do not have the `classes.dex` directly, you can extract it from the APK:

* MD5: `a10fae2ad515b4b76ad950ea5ef76f72`
* SHA1: `a5fd87e902ac6eeb8b1f885976da38ff4d70b52`

Please make sure you re-name `classes.dex` to `hesperbot.dex` and place it into the `/assets` folder before using the cracker.

It probably works with other versions of Hesperbot too but was never tested.

Usage
=====

Open the Project in Eclipse and compile it to an APK. Install it on a device of your choise, even the emulator works fine!


Presentation
============

We held a presentation about this Cracker at BSidesVienna 0x7DE which can be
found here: https://github.com/reox/bsidesvienna
