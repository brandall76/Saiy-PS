<a href="https://play.google.com/store/apps/details?id=ai.saiy.android" target="_blank">
  <img alt="Get it on Google Play"
       src="https://play.google.com/intl/en_us/badges/images/generic/en-play-badge.png" height="60"/>
</a>

# Saiy® for Android - Play Services Version

Here lies the open souce version of Saiy for Android, dependent on Google Play Services, which demonstrates how a Virtual Assistant functions, from start to finish.

## About

Saiy is a many times rebuilt version of its previous incarnation as utter! Countless attempts and therefore experience getting such an application to function on Android, has brought me to a point where I feel it's time to open source the code, for many reasons.

## Why Open Source

After spending a few years rewriting the code base, I think I have finally got it to a stage where it could be considered 'scalable'. That may not be the correct terminology, given the infinite possibilities of natural language requests and the finite amount of actions that could be coded to resolve them - Nevertheless, having adapted the application to integrate numerous APIs for Text to Speech, Speech to Text, Natural Language Understanding, Machine Learning, Cognitive Service (such as emotion analysis and vocal identification), and written my own APIs for developers to integrate their applications, functions and services, it's a case of now or never to open source it; whilst I hope it would currently be described as 'cutting edge'?

The project itself is too large and the possibilities it presents are too great for just a lone developer and it would be great to see what the community can do with it - iterating at a pace that no other similar application can keep up with, being held up by the shackles of corporate processes! And a ridiculous amount of unit tests...

Additionally, most of us find AI and the future of our virtual assistants and smart tech pretty fascinating, but to get involved requires a number of technical stepping stones. I hope by publishing this code and the ease of which commands can be created and adapted, using either simple String matching, a cloud based solution or your own NLP implementation, it will allow many to dive straight in and therefore further their interest.

## License & Copyright

The project is licensed under the GNU Affero General Public License V3. This is a copyleft license. See [LICENSE](https://github.com/brandall76/Saiy-PS/blob/master/LICENSE) 

I have selected this license over any other, in order to ensure that any adaptations or improvements to the code base, require to be published under the same license. This will protect any hard work from being adapted into closed sourced projects, without giving back.

The license grant is not for Saiy's trademarks, which include the logo designs. Saiy reserves all trademark and copyright rights in and to all [Saiy trademarks](https://trademarks.ipo.gov.uk/ipo-tmcase/page/Results/1/UK00003168669?legacySearch=False).

Copyright © 2017 Saiy® Ltd.

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
  
## Getting Started

The project is built using Java 7 - Android SDK (API 26) - Android NDK

Using Android Studio, it can be imported as a new project via version control or the downloadable zip.

There is a direct dependency to the Saiy Library project. Once that project is compiled you'll need to add the generated aar file as a module to the main project.

## Troubleshooting

Please use the stackoverflow tag for compiling questions and errors.

## Navigating the Code

Coming shortly
