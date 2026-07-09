#!/bin/bash
adb logcat -c && adb logcat | grep com.example.calorietracker
