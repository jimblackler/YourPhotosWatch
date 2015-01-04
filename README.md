Your Photos Watch
=================

I’ve just published a [new app for Android Wear smartphones to the Google Play Store](https://play.google.com/store/apps/details?id=net.jimblackler.yourphotoswatch). It’s called *Your Photos Watch* and it lets you view your personal photos as backgrounds to a specially-designed watch face.

Project
=======

I [wrote previously](http://jimblackler.net/blog/?p=419) about my first Android Wear watch face. With that project done I turned to something a little more complex. When I first received my smartwatch (a [LG G Watch](http://www.lg.com/global/gwatch/one)), I thought it might be nice to put some family photos on it - like a high-tech version of the photo in the wallet. I was a little disappointed to find that this isn’t something supported out-of-the-box. Nor could I find any photo apps on the Play store, using the obvious keywords.

This seemed like a missed opportunity as this is something that a smartwatch can uniquely offer. As an app developer there was only one thing to do; make such an app myself.

Making the app you want to see is an approach I would recommend to anyone looking to get started with app development. Think of those times you go looking for an app to do a particular thing and don’t find one that works the way you want. Build that app and get it out there! Others will find it useful too, I guarantee it!

App design
==========

I asked myself what form the app should take. A simple picture viewer was one idea, but traditional apps aren't really a good fit for the tiny screen of smartwatch. A custom watch face seemed like a better approach. The app could show a fresh picture each time the watch wakes from ambient mode (done by the watch when you rotate it to look at it). The user could make a selection photos to show using the companion phone app.

Data
====

The first technical hurdles were to get the photos from the phone app to the watch face app, and to keep them stored there.

In my first experiments I attempted to download photos directly from the internet on the device. I soon found out that this isn’t supported - directly at least - even though it could have been, since the watch is effectively tethered to the phone, which could proxy an internet connection. I realized this was probably by design, to encourage a thoughtful economy of internet use. I studied the intention of the Android Wear design to see how my plans could be adapted to gel with the API designers’ intentions.

I studied the [Data API](http://developer.android.com/training/wearables/data-layer/index.html) and after some experiments it was clear that creating a [Data Item](https://developer.android.com/training/wearables/data-layer/data-items.html) per photograph would allow me to synchronize from phone to watch. In addition experiments showed that the Data Items persist as long as the companion app remains installed on the phone. This means that in practice the Data API can act as long-term storage for the photos. The watch face and companion apps simply query for all Data Items to see which photographs have been selected by the user.

Cropping
========

One issue had yet to be solved. I was expecting to have to down-scale pictures to the resolution of the watch. However, the screen resolution of the smart watches is in a square aspect where most photographs are something like 4:3. It would not be possible to show the whole picture on the full screen of the watch. Some ‘letterboxing’ could be applied to show the entire picture but on an already small watch face this means diminished visual impact. I’d rather crop the photos, but this means typically one quarter of the area of the picture will have to be cut out.

Unfortunately taking the center square of the photo will often crop out the subject of the photo. A common case is when the subject is standing, in a vertical or ‘portrait’ format picture. The face is often in the top quarter of the frame; not the center.

Here’s an example of a landscape format photograph where the subject of the photograph (some strange looking software engineer) is not in the center of the frame. If this photo was cropped for a watch face by taking the center square, half of the subject’s face would be cut from the picture, which in this would be terrible.

The most obvious option would be to have the user select the area to crop in the companion app. However this adds an extra step for users to complete. A streamlined user experience is always better.

Face detection
--------------

My more ambitious idea was to use some off-the-shelf computer image recognition software to find the faces in each picture. When faces were found the cropped square can be adjusted to ensure as many as possible were in the chosen area.

It says something about technology today when such a computationally-intensive task can be performed on demand on a mobile phone, but a quick web search revealed that this was indeed feasible. The free software application called [Open CV](http://opencv.org) emerged as the most likely candidate. An Android port is available and many developers seem to be using it.

I had one or two practical issues importing the latest version into the build and getting the face-finding classifiers I had loaded and working. (Check the AutoCropper class in the [files in the project on GitHub](https://github.com/jimblackler/YourPhotosWatch/blob/master/Application/src/main/java/net/jimblackler/yourphotoswatch/AutoCropper.java) if you’re curious). Some tweaking of the input parameters was needed to get the balance between false negatives (faces not recognized) and processing time right.

I was pleased to arrive at a solution that works to my satisfaction around 19 times out of 20. The great advantage is the user has to do nothing to have their photos correctly cropped. In fact I would wager that the majority of users never even think about the cropping process. It will work as expected with no involvement from. Exactly how a good app should be.

Visual design
=============

Making this app I spent a great deal of time tweaking the watch face design. I thought about exposing settings to the user to change the visual appearance. If enough people ask me I might add this in later versions, but design-wise this can be something of a cop out. I would prefer to ship the app with a design that made a strong statement and that I felt worked within the design constraints of the app.

The main problem is this; the watch face should communicate the time quickly and clearly. If it doesn’t, it is failing in its primary function as a watch face, and once the novelty of the photo feature is worn off people will switch to a more easily-read watch.

On the other hand, the design should not cover too much of the user’s photo. If it does, the unique selling proposition of the app is lost.

Yet these two requirements conflict. A bolder design will be easier to read but will obscure too much of the user’s photos.

Faces
=====

I decided to include both a digital and traditional “analog” watch face.

The analog face took by far the most experimentation. The first problem is choice of colors. These shouldn't be too similar to the background photograph color or the watch face will be unreadable. As I can’t predict the colors of the photographs selected by users this is a problem.

A trick for showing text of other details over a multi-colored background is to use two colors side-by-side that contrast from each other. That way you have created an internal contrast in the image, meaning it should be readable whatever the background is. In the case of the watch face I found a white outline a few pixels thick coupled with a dark blue fill color achieves this contrast. Because the effect is quite striking I softened the design with round rather than square edges on ticks and hands. The high contrast allows the hands to be drawn slightly semi-transparent (around the 80% opaque mark) to subtly reveal a small amount of extra detail under the hands and tick marks.

The application of a dark drop shadow creates even more contrast and creates a subtle three-dimensional effect with the watch face over the background picture.

The digital face was a lot more straightforward. I chose to display hours and minutes in a large-ish Roboto font in the bottom half of the screen. Because it occupies relatively little of the screen the risk of obscuring the user photos is low. The same contrast-enhancing methods were used in the analog face.

Data sources
============

As the user has to supply photos for the watch face, I needed to work out where these would come from. The most obvious and easiest source is the photos that the user has already stored on their phone (e.g. ones they have taken with their phone camera).

Phone
-----

A query of images in the [Media Store](http://developer.android.com/reference/android/provider/MediaStore.Images.Media.html) handles this easily, once the relevant permission is added to the manifest.

[Two](https://github.com/jimblackler/YourPhotosWatch/blob/master/Application/src/main/java/net/jimblackler/yourphotoswatch/PhoneSelectActivity.java) [classes](https://github.com/jimblackler/YourPhotosWatch/blob/master/Application/src/main/java/net/jimblackler/yourphotoswatch/PhonePhotoListEntry.java
) handle the import of photos and handling in a Recycler Adapter. (This project marked the first time I've used the new [Recycler View](https://developer.android.com/training/material/lists-cards.html) on an Android project.)

One nice feature of this API is that the pictures can be requested in a (couple of thumbnail sizes)[http://developer.android.com/reference/android/provider/MediaStore.Images.Thumbnails.html]. The MINI_KIND size approximates at 512 x 384, which is actually larger than the typical target watch size of 320 x 320. That way my app doesn't have to downscale massive camera images itself.

Google
------

Given that any user with access to the play store will have their phone connected to a Google account, this suggested another obvious photo source. Interestingly there is no modern API to access photos shared on Google Plus, or photos that might have been save with the photos backup feature from one of the user’s previous Android phones. However these photos *can* be obtained from the aging [Picasa API](https://developers.google.com/picasa-web/docs/2.0/developers_guide_protocol). I say aging, because this uses an earlier version of the GData API and by default returns XML.

This part of the operation turned out to be the trickiest parts of the project, something I had not anticipated at all.

I made several wrong turns trying to parse the XML, for instance by reading a document and using XPath expressions. After a lot of annoyances with namespaces in the XML, I got it working, just. However long delays in the app showed that this is simply far too slow to be done on the phone when 100s of photos are being processed.

Then I tried to use the official [GData API Android libraries](https://developers.google.com/picasa-web/code). However this took my app over the [65,535 symbol limit](https://code.google.com/p/android/issues/detail?id=20814) (Dex limit). I experimented with various ways to work around this such as ProGuard rules to strip unused content, and multi-Dex mode. However ProGuard was breaking OpenCV by stripping symbols it was using (the large amount of native code in OpenCV complicated this). I spent many hours trying to get the ProGuard rules right before deciding I should move on. Multi-dex mode was similarly troublesome.

I then went back to parsing the XML myself, this time using a faster “forward parser” (aka [SAX](http://docs.oracle.com/javase/tutorial/jaxp/sax/parsing.html)) which interprets the XML stream without building a full version of the document in mmeory (a 'DOM'). This approach is faster but the code is considerably harder to develop and read than XPaths. This probably would have worked but I stubbornly wanted to separate the code that handled the parsing of each photo fragment into a different class, to match the existing structure of the code. Unfortunately, Java’s SAX implementation has a ‘push’ structure - the parser controls flow and calls back a handler with information about the file data - rather than a ‘pull’ structure where the main program would call back the parser to iterate over the data. This means the whole file has to be dealt with by a single handler. I looked around for an XML parser with a ‘pull’ approach but again I felt that I was really wasting time.

Then the penny dropped; I vaguely recalled that older Google feeds can on demand return a form of XML translated into JSON. JSON has a wider selection of parsers on Android including a ‘pull’ parser; [android.util.JsonReader](http://developer.android.com/reference/android/util/JsonReader.html). Sure enough, adding ‘alt=json’ to the feed URL returned a translated feed. I could have the best of both worlds; my code could be structured the way I wanted, and a reasonable performance could be obtained too.

Authentication to get access to a user's private data was the remaining challenge. For Google GData services it is possible to authenticate with the aid of the phone's account manager. There is something of a back-and-forth (sometimes called the 'OAuth2 dance') to get the user's email address (required) and an up-to-date access token to pass in to the API.

Facebook
--------

I could have gone on to add access to lots of other services such as Dropbox, Flikr, Instagram and more. If this app gets super-popular and I get requests to do so, I might. However, there was one obvious candidate given its popularity on Android and its history as a photo-sharing service, and that’s Facebook. By allowing import of photos shared in Facebook I could increase further users chances of finding treasured photos for the watch service (and get some useful experience in the API for myself too!)

The last time I dealt with Facebook as a developer was at work, some time around 2009, when I added some integration widgets to the AdSense publisher controls. I remember this being reasonably straightforward, but even so taking a new look five years later I was amazed; Facebook’s developer experience is incredibly slick, for Android at least. They supply an [Android library](https://developers.facebook.com/docs/android/getting-started) that integrates with the Facebook Android app making user management (signing in and out)
, authentication and data collection incredibly easy. I had it running quite quickly; the most complex part was some logic to select the most appropriate-sized version of images from the selection offered.

Another hurdle was that apps that request permission to view user photos need to be manually reviewed by Facebook to ensure they meet the terms of use and offer a good user experience. This was understandable but added a little paperwork and worry (for one thing, would the reviewers even have an Android Wear device to hand to test the app?) In the event, they quickly approved my request.

The app
=======

Like all my apps it’s completely free. It’s (on the Play Store)[https://play.google.com/store/apps/details?id=net.jimblackler.yourphotoswatch] from today. This one is licensed under Apache 2, and the source (can be found on GitHub)[https://github.com/jimblackler/YourPhotosWatch].

Feel free to get in touch on jimblackler@gmail.com if you have any thoughts or queries about the app.
