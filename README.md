# Solitare

A fork of the famous ad free Solitaire Game Suite from Ken Magic. This 
is an intial import from where Mr. Magic left off. There is another 
fork in existence, with various changes, some opposed stylistic.

The idea behind this fork is to update the app and keep it in sync with 
current Android SDK/NDK. This is a very early fork, no build 
instructions or other at this time beyond original below.

Contributions will be welcome as things progress.

## Original README

Simple solitaire game.

Currently documentation is limited but I will add comments to the source 
and detail the structure in here. 

Build Instructions:
------------------
I do not use eclipse so I can't really guarantee that it will build in 
that environment. 

First off you need to download the SDK from http://code.google.com/android.
Once installed you need to modify solitaire's build.xml file to include 
the path to the SDK (Two lines near the top). Then running ant should 
complete the job. If you have difficulties, try building a sample 
program from code.google.com first which has a bunch of detail and 
troubleshooting info.


