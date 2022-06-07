
# Android SMS Gateway  
  
This is recreate from [Old SMS Gateway](https://github.com/anjlab/android-sms-gateway)  
now using Firebase  
to turn my android as sms sender

# HOW IT WORKS

Sending SMS

1. You send data to sms.ibnux.net (or your server)
2. Server will send push notification
3. App receive push notification, and route it to sms
4. App receive sent notification, and post it to your server
5. App receive delivered notification, and post it to your server

RECEIVE SMS
1. App receive SMS
2. App send it to your server
  
# HOW TO USE?  
  
Download APK from [release](https://github.com/ibnux/Android-SMS-Gateway/releases) page  
  then open https://sms.ibnux.net/ to learn how to send sms

you can find backend folder for server side in this source

to compile yourself, you need your own Firebase

# FEATURES

- SENDING SMS
- RECEIVE SMS to SERVER
- SENT NOTIFICATION to SERVER
- DELIVERED NOTIFICATION to SERVER
- USSD
- MULTIPLE SIMCARD
- RETRY SMS FAILED TO SENT 3 TIMES

## USSD Request

Not all phone and carrier work with this, this feature need accessibility to read message and auto close USSD dialog, but some device failed to close Dialog, i use samsung S10 Lite and it cannot close dialog

## MULTIPLE SIMCARD

i think not all phone will work too, because of different of API for some OS which vendor has modification

# Install on your own Server?

You need to understand how to build android Apps, and compile your own version.

Create Firebase project, add apps to project to get google-services.json

Add server key to **backend** script

You will see MyObjectBox error, just build it once, it will create automatically, read in [here](https://docs.objectbox.io/getting-started#generate-objectbox-code)

***

## Traktir @ibnux

[<img src="https://ibnux.github.io/KaryaKarsa-button/karyaKarsaButton.png" width="128">](https://karyakarsa.com/ibnux)

[<img src="https://ibnux.github.io/Trakteer-button/trakteer_button.png" width="120">](https://trakteer.id/ibnux)

## DONATE @ibnux

[paypal.me/ibnux](https://paypal.me/ibnux)

# LICENSE  
## Apache License 2.0  
  
Permissions  
  
    ✓ Commercial use  
    ✓ Distribution  
    ✓ Modification  
    ✓ Patent use  
    ✓ Private use  
  
Conditions  
  
    License and copyright notice  
    State changes  
  
Limitations  
  
    No Liability  
    No Trademark use  
    No Warranty  
  
you can find license file inside folder
