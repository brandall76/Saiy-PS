<a href="https://play.google.com/store/apps/details?id=ai.saiy.android" target="_blank">
  <img alt="Get it on Google Play"
       src="https://play.google.com/intl/en_us/badges/images/generic/en-play-badge.png" height="60"/>
</a>

# Saiy® for Android - Play Services Version

Here lies the open source version of Saiy for Android, dependent on Google Play Services, which demonstrates how a Virtual Assistant functions, from start to finish.

## About

Saiy is a many times rebuilt version of its previous incarnation as utter! Countless attempts and therefore experience getting such an application to function on Android, has brought me to a point where I feel it's time to open source the code, for many reasons.

## Why Open Source?

After spending a few years rewriting the code base, I think I have finally got it to a stage where it could be considered 'scalable'. That may not be the correct terminology, given the infinite possibilities of natural language requests and the finite amount of actions that could be coded to resolve them - Nevertheless, having adapted the application to integrate numerous APIs for Text to Speech, Speech to Text, Natural Language Understanding, Machine Learning, Cognitive Services (such as emotion analysis and vocal identification) (in the least possible spaghetti way I could achieve) and written my own APIs for developers to integrate their applications, functions and services, it's a case of now or never to open source it; whilst the implementations to these connected services and APIs are functional and up-to-date.

The project itself is too large and the possibilities it presents are too great for just a lone developer and it would be great to see what the community can do with it - iterating at a pace that no other similar application can keep up with.

Additionally, most of us find AI and the future of our virtual assistants and smart tech pretty fascinating, but to get involved requires a number of technical stepping stones. I hope by publishing this code and the ease of which commands can be created and adapted, using either simple String matching, a cloud based solution or your own NLP implementation, it will allow many to dive straight in and therefore further their interest.

## License & Copyright

The project is licensed under the GNU Affero General Public License V3. This is a copyleft license. See [LICENSE](https://github.com/brandall76/Saiy-PS/blob/master/LICENSE) 

I have selected this license over any other, in order to ensure that any adaptations or improvements to the code base, require to be published under the same license. This will protect any hard work from being adapted into closed sourced projects, without giving back.

The license grant is not for Saiy's trademarks, which include the logo designs. Saiy reserves all trademark and copyright rights in and to all [Saiy trademarks](https://trademarks.ipo.gov.uk/ipo-tmcase/page/Results/1/UK00003168669?legacySearch=False).

Copyright © 2017 Saiy® Ltd.

## Contributor License Agreements

I need to clarify the most appropriate for the GNU Affero General Public License - will revisit very soon. Any suggestions welcome.

## Features

- Network & Embedded Text to Speech
  - Embedded Android text to speech
  - Nuance text to speech
  
  See Saiy TTS project for further integration examples.
  
- Network & Native Speech to Text
  - Google native Android voice recognition
  - Nuance voice recognition
  - Microsoft voice recognition
  - IBM voice recognition
  - WIT voice recognition
  
- Offline Hotword
  - PocketSphinx
  
- Natural Language Processing
  - API.ai
  - Wit.ai
  - Nuance mix
  - Microsoft
  - IBM Bluemix
  
- Vocal Identification
  - Microsoft (Project Oxford)
  
- Emotion Analysis
  - Beyond Verbal
  
- Knowledge Base
  - Wolfram Alpha
  
- Developer APIs
  - [Example App](https://github.com/brandall76/API-Example-App)
  
## Getting Started

The project is built using Java 7 - Android SDK (API 26) - Android NDK

Using Android Studio, it can be imported as a new project via version control or the downloadable zip.

There is a direct dependency to the [Saiy Library project](https://github.com/brandall76/Saiy-Library). Once that project has compiled you'll need to add the generated aar file as a module to the main project, as described [here](https://stackoverflow.com/q/29826717/1256219).

Without stating the obvious, when testing on a physical device, the performance of the code is accentuated by the hardware specifications - more so than your average app, as there is a lot going on. 

Installing the [Google Text to Speech Engine](https://play.google.com/store/apps/details?id=com.google.android.tts&hl=en_GB) on your test device is recommended, due to the features it provides.

To use free embedded and offline Voice Recognition, install [Google's 'Now'](https://play.google.com/store/apps/details?id=com.google.android.googlequicksearchbox&hl=en_GB) application. If you have a Samsung device, their Vlingo recognition service **does not** work correctly for external applications.

## Providers

 - [Nuance Developers](https://developer.nuance.com) - Text to Speech - Speech to Text - NLU 
 - [Microsoft Cognitive Services](https://azure.microsoft.com/en-gb/services/cognitive-services/) - Text to Speech - NLU - Translate API
 - [IBM Bluemix](https://www.ibm.com/watson/services/speech-to-text/) - Speech to Text  - NLU
 - [Wit](https://wit.ai/) - Speech to Text - NLU
 - [PocketSphinx](https://github.com/cmusphinx/pocketsphinx-android-demo) - Speech to Text
 - [Google Cloud Speech](https://cloud.google.com/speech/) - Speech to Text
 - [Google Chromium Speech](https://www.chromium.org/developers/how-tos/api-keys) - Speech to Text
 - [API AI](https://api.ai/) - Speech to Text - NLU
 - [Beyond Verbal](http://developers.beyondverbal.com) - Emotion analytics
 - [Wolfram Alpha](https://developer.wolframalpha.com/portal/signin.html) - Knowledge base

## Troubleshooting

Please use the [Stack Overflow tag](https://stackoverflow.com/tags/saiy/info) for compiling related questions and errors.

For code issues and crashes, please open an issue.

## Navigating the Code

In all major areas of the code, I will attempt to add further README files to detail a more specific explanation - including TO-DOs, issues and required improvements. Check the subdirectories of the code to see if a README is present, or a placeholder, letting you know that one should be soon.

Briefly, there are two major classes in the app, that direct and distribute work elsewhere:

- [SelfAware](https://github.com/brandall76/Saiy-PS/blob/master/app/src/main/java/ai/saiy/android/service/SelfAware.java) is the main Foreground Service, responsible for managing the application state and channelling voice recognition, text to speech and other API requests.

- [Quantum](https://github.com/brandall76/Saiy-PS/blob/master/app/src/main/java/ai/saiy/android/processing/Quantum.java) is the main processing class, where commands are locally resolved (if required), sensibility checked and actioned.

Understanding the above two classes is essential to following the flow of the full application logic.

- [MyLog](https://github.com/brandall76/Saiy-PS/blob/master/app/src/main/java/ai/saiy/android/utils/MyLog.java#L41) is a global verbose logging toggle. When enabled, the output will flow class to class, as well as display durations for time sensitive functions. 

## Credentials

For the sake of testing ease, the code points to static API keys and secrets held in the [configuration directory](https://github.com/brandall76/Saiy-PS/tree/master/app/src/main/java/ai/saiy/android/configuration). It should probably go without saying, don't do this in production code.

## Strings for offline language processing?

This dates back to my original code written as a beginner for utter! That said, it is only relatively recently that cloud services such as API.ai or [embedded options](https://github.com/tensorflow/tensorflow/blob/master/tensorflow/examples/android/src/org/tensorflow/demo/SpeechActivity.java) became available/usable as well as manageable for an individual developer.

Up until this point, Java libraries that attempted the equivalent, bloated and lagged the app to the point of stalling. They were not an option.

I am also mindful, that I would like developers of any experience to be able to contribute to and manipulate the code with ease. Basic String operations can be converted by others over at [SaiyeyMcSaiyface](https://github.com/brandall76/SaiyeyMcSaiyface) 

## Theory

I use the word, scalability, with caution. Whatever strides machine learning has taken up until now, there is still a requirement for a human to hard-code the ultimate action that is resolved to be performed. Whether this be turning up your heating, or the generic layout design of a weather request, someone still needs to write that code and the surrounding error handling.

Whilst standardised templates, to organise the world of information around us, can assist to categorise the output to a set of static response mechanisms - we perhaps can't feasibly use the word scalable, until a machine can dynamically write code (or the equivalent) for itself.

The above is for another discussion, but the point to take is that development is currently consigned to the following:

- Receive a command request
  - No further explanation needed
- Resolve the command to collection
  - Bluetooth on/off/toggle - would be a 'Bluetooth collection'
  - Weather conditions in location on date - would be 'Weather collection'
  - None
- Apply sensibility/boundary checks
  - Turn the oven on to 3000 degrees for 8 years
  - Remind me to go to the doctor yesterday
  - What's the weather like in Primark
  - Spell a
  - Drive me over Niagara Falls
- Action the request
  - Deducing success/error/insufficiency
  - Extract standardisation - _{ location:"Berlin" } { description:"light drizzle" }_
  
Much of the above will need to be hard-coded. I state this only to manage expectations of currently how 'smart' we can hope to be...

## What's the plan?

Initially, I have published only the core of the application, so it may be critiqued in terms of its structure and quality of code. Much of the fundamental construct of the app and the code style/quality used, is repeated across the 500K+ lines still to be pushed.

Once there is general consensus on the application core, I will begin to upload the remaining code, with any suggested alterations already in place.

I am entirely self taught in Java, so go easy on me!

- Fundamentals
  - Generally, how the construct of the application and way it interconnects can be improved. Both in terms of performance and readability.

- Memory management
  - Given that the application functions as a Foreground service, great detail must to be paid to memory allocation. Theoretically, the application could persist forever - there is no luxury of onDestroy() to wipe the footprint. The Garbage Collector is nudged at various points throughout the application, when micro delays are of no concern.

- Threading strategy
  - Whilst I've been mindful of how the threading functions across the code, using standard practices in terms of background/foreground/priority/pools etc - there remains no specific 'strategy' as to how these are managed. I would appreciate input from others here. 

- Micro-optimisation
  - I've no issue with accepting pull requests for this type of optimisation. I'm looking forward to seeing how contributors can shave off a thousandth here and there. Please do provide a test to back-up any submissions.

- Unit tests
  - My own tests were not worthy of publishing. If this is your thing, help!
  
- Translation
  - I hope to translate the application into every language available via both a voice recognition and text to speech provider. I'm using the [Crowdin Plaform](https://crowdin.com/project/saiy/invite?d=d5b5m4057517e563r44323n463) if you'd like to help.
  
- Visualisation
  - Since API 23 introduced a comprehensive [Assistant Framework](https://developer.android.com/training/articles/assistant.html), visualising data has become an easier prospect. I have no current implementation of this, but it's now on the roadmap.
  - An augmented reality visualisation of Saiy built using [ARCore](https://github.com/google-ar/arcore-android-sdk) can be found [here](https://github.com/brandall76/Saiy-AR).
  
- Localisation
  - If a user is controlling Saiy in a language other than their native (presumably due to it being unsupported), standard String resource management, based on their device Locale, will point to the wrong destination and therefore fail. The resolution of this process is done using a [SupportedLanguage](https://github.com/brandall76/Saiy-PS/blob/master/app/src/main/java/ai/saiy/android/localisation/SupportedLanguage.java) object. Further explanation can be found in the [localisation directory](https://github.com/brandall76/Saiy-PS/tree/master/app/src/main/java/ai/saiy/android/localisation) README.
  
## Java 8
  
The code was originally written in Java 8, but had to be reverted due to build [issues with the Jack compiler](https://issuetracker.google.com/issues/37127783)

Now [Jack is deprecated](https://source.android.com/source/jack), I plan to revisit this soon.
