# What is it
  This is a tiny Android app contains 2 primary functions: proxy and packet capturing, with no root requirement.
  1) Proxy
     
     Some kind of like ShadowSocket, You can specify the network request made by other apps on your phone to go through the proxy server you designated. 
  2) Packet capturing
     
     It can capture IP packets and store in a pcap format which can be opened by wireshark.
     
# How to use it

  1. Download [apk](https://github.com/SummerOak/NetCloud_android/releases/download/v1.0/NetCloud.apk) and install it on your Android phone;
  
  2. Add apps you need proxy or capturing into traffic control, and you may need add your proxy server in settings panel if you are using proxy:
      
      <img src="https://github.com/SummerOak/NetCloud_android/blob/master/screenshorts/select_apps.png" alt="drawing" width="200"/>
      
      <img src="https://github.com/SummerOak/NetCloud_android/blob/master/screenshorts/control_panel.png" alt="drawing" width="200"/>
      
      <img src="https://github.com/SummerOak/NetCloud_android/blob/master/screenshorts/settings_panel.png" alt="drawing" width="200"/>
    
     
     
  3. Click start button on the main page to start traffic control;   
  
      <img src="https://github.com/SummerOak/NetCloud_android/blob/master/screenshorts/main_page.png" alt="drawing" width="200"/>
      
      <img src="https://github.com/SummerOak/NetCloud_android/blob/master/screenshorts/proxy.png" alt="drawing" width="200"/>
  
  4. Packet capturing will stop if you click stop or the size of pcap file exceeded limitation(10MB). After packet capturing stoped, the pcap file will be located in the path configed in settings panel, pull it out and open it with wireshark or charles;
     
     <img src="https://github.com/SummerOak/NetCloud_android/blob/master/screenshorts/wire_shark.png" alt="drawing"/>
  
    
  5. Finally, if you want specify proxy server to your own, you need run the [server](https://github.com/SummerOak/NetSword) on your proxy server;
