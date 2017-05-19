# LiveRadioSongMatching
Match between a live playing song on a device vs. metadata on the device, web or feed

This project opens a device (iOS) and web browser and
1- record a song from iHeart radio application (20 seconds)
2- recognize the song (using ACRCloud.com APIs)
3- extract from the web browser the artist and song names
4- compare that against the song meta data as provided back from ACRCloud (step 2)

Please ensure to add to the environment variables:
PERFECTO_CLOUD (ex.: demo.perfectomobile.com)
PERFECTO_CLOUD_USERNAME
PERFECTO_CLOUD_PASSWORD

ACR_Access_Key: Key to ACRCloud, get it at https://www.acrcloud.com, follow this guide: https://www.acrcloud.com/docs/tutorials/identify-music-by-sound/
ACR_Host
ACR_Secret

Project_Path: where you store downloaded wav file and the .dylib file

